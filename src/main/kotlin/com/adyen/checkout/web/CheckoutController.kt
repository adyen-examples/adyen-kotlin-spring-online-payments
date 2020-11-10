package com.adyen.checkout.web

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam

@Controller
class CheckoutController {
    @Value("\${ADYEN_CLIENT_KEY}")
    private val clientKey: String? = null

    @GetMapping("/")
    fun index(): String {
        return "index"
    }

    @GetMapping("/preview")
    fun preview(@RequestParam type: String?, model: Model): String {
        model.addAttribute("type", type)
        return "preview"
    }

    @GetMapping("/checkout")
    fun checkout(@RequestParam type: String?, model: Model): String {
        model.addAttribute("type", type)
        model.addAttribute("clientKey", clientKey)
        return "checkout"
    }

    @GetMapping("/result/{type}")
    fun result(@PathVariable type: String?, model: Model): String {
        model.addAttribute("type", type)
        return "result"
    }
}