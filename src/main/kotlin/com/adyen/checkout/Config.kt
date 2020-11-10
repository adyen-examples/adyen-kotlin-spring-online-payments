package com.adyen.checkout

import nz.net.ultraq.thymeleaf.LayoutDialect
import org.springframework.context.annotation.Bean

@Bean
fun layoutDialect(): LayoutDialect {
    return LayoutDialect()
}
