package com.adyen.checkout.api

import com.adyen.Client
import com.adyen.Config
import com.adyen.checkout.AdyenConfig
import com.adyen.enums.Environment
import com.adyen.model.checkout.Amount
import com.adyen.model.checkout.CreateCheckoutSessionRequest
import com.adyen.model.checkout.CreateCheckoutSessionResponse
import com.adyen.model.checkout.LineItem
import com.adyen.service.checkout.PaymentsApi;
import com.adyen.service.exception.ApiException
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.IOException
import java.util.*


/**
 * REST controller for using Adyen checkout API
 */
@RestController
@RequestMapping("/api")
class CheckoutResource @Autowired constructor(private val adyenConfig: AdyenConfig) {
    private val log = LoggerFactory.getLogger(CheckoutResource::class.java)

    private val paymentsApi: PaymentsApi? by lazy {
        val apiKey = adyenConfig.apiKey
        if (apiKey.isNullOrBlank()) {
            log.warn("ADYEN_API_KEY is not configured. Checkout API will not be available.")
            null
        } else {
            val config = Config()
            config.apiKey = apiKey
            config.environment = Environment.TEST
            config.applicationName = "[adyen-kotlin-spring-online-payments checkout-example adyen-web/5.68.0]"
            val client = Client(config)
            PaymentsApi(client)
        }
    }

    @PostMapping("/sessions")
    @Throws(IOException::class, ApiException::class)
    fun sessions(@RequestHeader host: String, @RequestParam type: String?, request: HttpServletRequest): ResponseEntity<CreateCheckoutSessionResponse> {
        val api = paymentsApi
        if (api == null) {
            log.error("ADYEN_API_KEY is not configured. Cannot create checkout session.")
            return ResponseEntity.status(500).build()
        }

        val merchantAccount = adyenConfig.merchantAccount
        if (merchantAccount.isNullOrBlank()) {
            log.error("ADYEN_MERCHANT_ACCOUNT is not configured. Cannot create checkout session.")
            return ResponseEntity.status(500).build()
        }

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
        val response = api.sessions(checkoutSession)
        return ResponseEntity.ok().body(response)
    }
}
