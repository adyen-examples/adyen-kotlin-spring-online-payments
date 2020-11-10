package com.adyen.checkout

import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class OnlinePaymentsApplication

private val log = LoggerFactory.getLogger(OnlinePaymentsApplication::class.java)

fun main(args: Array<String>) {
    runApplication<OnlinePaymentsApplication>(*args)
    log.info("\n----------------------------------------------------------\n\t" +
            "Application is running! Access URLs:\n\t" +
            "Local: \t\thttp://localhost:8080\n\t" +
            "\n----------------------------------------------------------")
}

