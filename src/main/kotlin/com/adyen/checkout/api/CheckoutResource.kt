package com.adyen.checkout.api

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.CreateCheckoutSessionRequest
import com.adyen.model.checkout.CreateCheckoutSessionResponse
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
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
    fun sessions(@RequestParam type: String?): ResponseEntity<CreateCheckoutSessionResponse> {
        val orderRef = UUID.randomUUID().toString()
        val amount = Amount()
            .currency("EUR")
            .value(1000L) // value is 10€ in minor units
        val checkoutSession = CreateCheckoutSessionRequest()
        checkoutSession.merchantAccount(merchantAccount)
        checkoutSession.channel = CreateCheckoutSessionRequest.ChannelEnum.WEB
        checkoutSession.reference = orderRef // required
        checkoutSession.returnUrl = "http://localhost:8080/redirect?orderRef=$orderRef"
        checkoutSession.amount = amount
        log.info("REST request to create Adyen Payment Session {}", checkoutSession)
        val response = checkout.sessions(checkoutSession)
        return ResponseEntity.ok().body(response)
    }
}
