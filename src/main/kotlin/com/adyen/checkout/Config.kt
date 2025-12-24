package com.adyen.checkout

import nz.net.ultraq.thymeleaf.layoutdialect.LayoutDialect
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

/**
 * Configuration component for Adyen environment variables.
 * Allows the application to start even when environment variables are not provided.
 */
@Component
class AdyenConfig {
    private val log = LoggerFactory.getLogger(AdyenConfig::class.java)

    @Value("\${ADYEN_API_KEY:}")
    val apiKey: String? = null

    @Value("\${ADYEN_CLIENT_KEY:}")
    val clientKey: String? = null

    @Value("\${ADYEN_MERCHANT_ACCOUNT:}")
    val merchantAccount: String? = null

    @Value("\${ADYEN_HMAC_KEY:}")
    val hmacKey: String? = null

    init {
        logConfigurationStatus()
    }

    private fun logConfigurationStatus() {
        val missing = mutableListOf<String>()
        
        if (apiKey.isNullOrBlank()) missing.add("ADYEN_API_KEY")
        if (clientKey.isNullOrBlank()) missing.add("ADYEN_CLIENT_KEY")
        if (merchantAccount.isNullOrBlank()) missing.add("ADYEN_MERCHANT_ACCOUNT")
        if (hmacKey.isNullOrBlank()) missing.add("ADYEN_HMAC_KEY (optional, recommended for webhooks)")

        if (missing.isNotEmpty()) {
            log.warn(
                "Some Adyen environment variables are not set: {}. " +
                "The application will start but certain features may not work correctly.",
                missing.joinToString(", ")
            )
        } else {
            log.info("All Adyen environment variables are configured")
        }
    }
}

@Bean
fun layoutDialect(): LayoutDialect {
    return LayoutDialect()
}
