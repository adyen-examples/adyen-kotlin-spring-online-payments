package com.adyen.checkout.api

import com.adyen.model.notification.NotificationRequest
import com.adyen.model.notification.NotificationRequestItem
import com.adyen.util.HMACValidator
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.security.SignatureException
import java.util.function.Consumer

/**
 * REST controller for receiving Adyen webhook notifications
 */
@RestController
@RequestMapping("/api")
class WebhookResource @Autowired constructor(@Value("\${ADYEN_HMAC_KEY}") key: String?) {
    private val log = LoggerFactory.getLogger(WebhookResource::class.java)

    private val hmacKey : String? = key

    init {
        if (hmacKey == null) {
            log.warn("ADYEN_HMAC_KEY is UNDEFINED (Webhook cannot be authenticated)")
            throw RuntimeException("ADYEN_HMAC_KEY is UNDEFINED")
        }
    }

    /**
     * Process incoming Webhook notifications
     * @param notificationRequest
     * @return
     */
    @PostMapping("/webhooks/notifications")
    fun webhooks(@RequestBody notificationRequest: NotificationRequest): ResponseEntity<String> {
        notificationRequest.notificationItems.forEach(
            Consumer { item: NotificationRequestItem ->
                // We recommend validate HMAC signature in the webhooks for security reasons
                try {
                    if (HMACValidator().validateHMAC(item, hmacKey)) {
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
                    } else {
//                         invalid HMAC signature: do not send [accepted] response
                        log.warn("Could not validate HMAC signature for incoming webhook message: {}", item)
                        throw RuntimeException("Invalid HMAC signature")
                    }
                } catch (e: SignatureException) {
                    log.error("Error while validating HMAC Key", e)
                }
            }
        )

        // Notifying the server we're accepting the payload
        return ResponseEntity.ok().body("[accepted]")
    }
}
