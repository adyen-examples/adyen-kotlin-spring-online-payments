package com.adyen.checkout.api

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.CreateCheckoutSessionRequest
import com.adyen.model.checkout.CreateCheckoutSessionResponse
import com.adyen.model.checkout.LineItem
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*


/**
 * REST controller for using Adyen checkout API
 */
@RestController
@RequestMapping("/api")
class CheckoutResource(@Value("\${ADYEN_API_KEY}") apiKey: String?) {
    private val log = LoggerFactory.getLogger(CheckoutResource::class.java)

    @Value("\${ADYEN_MERCHANT_ACCOUNT}")
    private val merchantAccount: String? = null

    private val checkout: Checkout

    init {
        val client = Client(apiKey, Environment.TEST)
        checkout = Checkout(client)
    }

    @PostMapping("/sessions")
    @Throws(IOException::class, ApiException::class)
    fun sessions(@RequestHeader host: String, @RequestParam type: String?, request: HttpServletRequest): ResponseEntity<CreateCheckoutSessionResponse> {

        val orderRef = UUID.randomUUID().toString()
        val amount = Amount()
            .currency("EUR")
            .value(10000L) // value is 100€ in minor units
        val checkoutSession = CreateCheckoutSessionRequest()
        checkoutSession.countryCode("NL");
        checkoutSession.merchantAccount(merchantAccount)
        checkoutSession.channel = CreateCheckoutSessionRequest.ChannelEnum.WEB
        checkoutSession.reference = orderRef // required
        checkoutSession.returnUrl = "${request.scheme}://${host}/redirect?orderRef=$orderRef"
        checkoutSession.amount = amount
        // set lineItems required for some payment methods (ie Klarna)
        checkoutSession.lineItems = listOf(
            LineItem().quantity(1L).amountIncludingTax(5000L).description("Sunglasses"),
            LineItem().quantity(1L).amountIncludingTax(5000L).description("Headphones")
        )

        log.info("REST request to create Adyen Payment Session {}", checkoutSession)
        val response = checkout.sessions(checkoutSession)
        return ResponseEntity.ok().body(response)
    }
}
