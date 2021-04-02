package com.adyen.checkout.api

import com.adyen.Client
import com.adyen.enums.Environment
import com.adyen.model.Amount
import com.adyen.model.checkout.*
import com.adyen.service.Checkout
import com.adyen.service.exception.ApiException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.servlet.view.RedirectView
import java.io.IOException
import javax.servlet.http.HttpServletRequest
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

    /**
     * `POST  /getPaymentMethods` : Get valid payment methods.
     *
     * @return the [ResponseEntity] with status `200 (Ok)` and with body the paymentMethods response.
     * @throws IOException  from Adyen API.
     * @throws ApiException from Adyen API.
     */
    @PostMapping("/getPaymentMethods")
    @Throws(IOException::class, ApiException::class)
    fun paymentMethods(): ResponseEntity<PaymentMethodsResponse> {
        val paymentMethodsRequest = PaymentMethodsRequest()
        paymentMethodsRequest.merchantAccount = merchantAccount
        paymentMethodsRequest.channel = PaymentMethodsRequest.ChannelEnum.WEB

        log.info("REST request to get Adyen payment methods {}", paymentMethodsRequest)
        val response = checkout.paymentMethods(paymentMethodsRequest)
        return ResponseEntity.ok()
                .body(response)
    }

    /**
     * `POST  /initiatePayment` : Make a payment.
     *
     * @return the [ResponseEntity] with status `200 (Ok)` and with body the paymentMethods response.
     * @throws IOException  from Adyen API.
     * @throws ApiException from Adyen API.
     */
    @PostMapping("/initiatePayment")
    @Throws(IOException::class, ApiException::class)
    fun payments(@RequestBody body: PaymentsRequest, request: HttpServletRequest): ResponseEntity<PaymentsResponse> {
        val paymentRequest = PaymentsRequest()
        paymentRequest.merchantAccount = merchantAccount // required
        paymentRequest.channel = PaymentsRequest.ChannelEnum.WEB // required

        val amount = Amount()
                .currency(findCurrency(body.paymentMethod.type))
                .value(1000L)  // value is 10€ in minor units
        paymentRequest.amount = amount

        val orderRef = UUID.randomUUID().toString()
        paymentRequest.reference = orderRef // required
        // required for 3ds2 redirect flow
        paymentRequest.returnUrl = "http://localhost:8080/api/handleShopperRedirect?orderRef=$orderRef"

        // required for 3ds2 native flow
        paymentRequest.additionalData = Collections.singletonMap("allow3DS2", "true")
        paymentRequest.origin = "http://localhost:8080"
        // required for 3ds2
        paymentRequest.browserInfo = body.browserInfo
        // required by some issuers for 3ds2
        paymentRequest.shopperIP = request.remoteAddr
        paymentRequest.paymentMethod = body.paymentMethod

        // required for Klarna
        if (body.paymentMethod.type.contains("klarna")) {
            paymentRequest.countryCode = "DE"
            paymentRequest.shopperReference = "1234"
            paymentRequest.shopperEmail = "youremail@email.com"
            paymentRequest.shopperLocale = "en_US"
            val lineItems = ArrayList<LineItem>()
            lineItems.add(
                    LineItem().quantity(1L).amountExcludingTax(331L).taxPercentage(2100L).description("Sunglasses").id("Item 1").taxAmount(69L).amountIncludingTax(400L)
            )
            lineItems.add(
                    LineItem().quantity(2L).amountExcludingTax(248L).taxPercentage(2100L).description("Headphones").id("Item 2").taxAmount(52L).amountIncludingTax(300L)
            )
            paymentRequest.lineItems = lineItems
        }

        log.info("REST request to make Adyen payment {}", paymentRequest)
        val response = checkout.payments(paymentRequest)
        return ResponseEntity.ok()
                .body(response)
    }

    /**
     * `POST  /submitAdditionalDetails` : Make a payment.
     *
     * @return the [ResponseEntity] with status `200 (Ok)` and with body the paymentMethods response.
     * @throws IOException  from Adyen API.
     * @throws ApiException from Adyen API.
     */
    @PostMapping("/submitAdditionalDetails")
    @Throws(IOException::class, ApiException::class)
    fun payments(@RequestBody detailsRequest: PaymentsDetailsRequest?): ResponseEntity<PaymentsDetailsResponse> {
        log.info("REST request to make Adyen payment details {}", detailsRequest)
        val response = checkout.paymentsDetails(detailsRequest)
        return ResponseEntity.ok()
                .body(response)
    }

    /**
     * `GET  /handleShopperRedirect` : Handle redirect during payment.
     *
     * @return the [RedirectView] with status `302`
     * @throws IOException  from Adyen API.
     * @throws ApiException from Adyen API.
     */
    @GetMapping("/handleShopperRedirect")
    @Throws(IOException::class, ApiException::class)
    fun redirect(@RequestParam(required = false) payload: String?, @RequestParam(required = false) redirectResult: String?, @RequestParam orderRef: String?): RedirectView {
        val detailsRequest = PaymentsDetailsRequest()
        if (redirectResult != null && redirectResult.isNotEmpty()) {
            detailsRequest.details = Collections.singletonMap("redirectResult", redirectResult)
        } else if (payload != null && payload.isNotEmpty()) {
            detailsRequest.details = Collections.singletonMap("payload", payload)
        }
        return getRedirectView(detailsRequest)
    }

    @Throws(ApiException::class, IOException::class)
    private fun getRedirectView(detailsRequest: PaymentsDetailsRequest): RedirectView {
        log.info("REST request to handle payment redirect {}", detailsRequest)
        val response = checkout.paymentsDetails(detailsRequest)
        var redirectURL = "/result/"
        redirectURL += when (response.resultCode) {
            PaymentsResponse.ResultCodeEnum.AUTHORISED -> "success"
            PaymentsResponse.ResultCodeEnum.PENDING, PaymentsResponse.ResultCodeEnum.RECEIVED -> "pending"
            PaymentsResponse.ResultCodeEnum.REFUSED -> "failed"
            else -> "error"
        }
        return RedirectView(redirectURL + "?reason=" + response.resultCode)
    }

    /* ################# UTILS ###################### */
    private fun findCurrency(type: String): String {
        return when (type) {
            "ach" -> "USD"
            "wechatpayqr", "alipay" -> "CNY"
            "dotpay" -> "PLN"
            "boletobancario", "boletobancario_santander" -> "BRL"
            else -> "EUR"
        }
    }
    /* ################# end UTILS ###################### */
}
