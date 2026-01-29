package com.adyen.checkout.api

import com.adyen.checkout.AdyenConfig
import com.adyen.model.notification.NotificationRequest
import com.adyen.model.notification.NotificationRequestItem
import com.adyen.util.HMACValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.SignatureException

/**
 * REST controller for receiving Adyen webhook notifications
 */
@RestController
@RequestMapping("/api")
class WebhookResource @Autowired constructor(private val adyenConfig: AdyenConfig) {
    private val log = LoggerFactory.getLogger(WebhookResource::class.java)

    private val hmacKey: String?
        get() = adyenConfig.hmacKey

    init {
        if (hmacKey.isNullOrBlank()) {
            log.warn("ADYEN_HMAC_KEY is UNDEFINED (Webhook cannot be authenticated)")
        }
    }

    /**
     * Process incoming Webhook notifications
     * @param notificationRequest
     * @return
     */
    @PostMapping("/webhooks/notifications")
    fun webhooks(@RequestBody json: String): ResponseEntity<String> {

        // from JSON string to object
        val notificationRequest = NotificationRequest.fromJson(json)

        // JSON and HTTP POST notifications always contain a single NotificationRequestItem object
	    // See also https://docs.adyen.com/development-resources/webhooks/understand-notifications#notification-structure
        notificationRequest.notificationItems.firstOrNull()?.let { item: NotificationRequestItem ->
            try {
                // We always recommend validating HMAC signature in the webhooks for security reasons, see https://docs.adyen.com/development-resources/webhooks/verify-hmac-signatures
                val key = hmacKey
                if (key.isNullOrBlank()) {
                    log.warn("ADYEN_HMAC_KEY is not configured. Skipping HMAC validation for webhook: {}", item)
                } else if (!HMACValidator().validateHMAC(item, key)) {
                    // Invalid HMAC signature
                    log.warn("Could not validate HMAC signature for incoming webhook message: {}", item)
                    throw RuntimeException("Invalid HMAC signature")
                }

                // Process the notification here, in this case we log it
                log.info(
                    """
                        Received webhook with event {} :
                        Merchant Reference: {}
                        Alias : {}
                        PSP reference : {}
                        """.trimIndent(),
                    item.eventCode,
                    item.merchantReference,
                    item.additionalData["alias"],
                    item.pspReference
                )

                // Acknowledge event has been consumed
                return ResponseEntity.status(HttpStatus.ACCEPTED).build()

            } catch (e: SignatureException) {
                log.error("Error while validating HMAC Key", e)
            }
        }

        return ResponseEntity.badRequest().body("[invalid request]")
    }
}
