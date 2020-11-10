package com.adyen.checkout.api

import com.adyen.model.checkout.DefaultPaymentMethodDetails
import com.adyen.model.checkout.PaymentMethodDetails
import com.adyen.model.checkout.details.AchDetails
import com.adyen.model.checkout.details.DotpayDetails
import com.adyen.model.checkout.details.GiropayDetails
import com.adyen.model.checkout.details.KlarnaDetails
// import com.adyen.model.checkout.details.SepaDirectDebitDetails;
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import org.springframework.boot.jackson.JsonComponent
import java.io.IOException

@JsonComponent
class PaymentMethodDetailsDeserializer : JsonDeserializer<PaymentMethodDetails>() {

    @Throws(IOException::class)
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext): PaymentMethodDetails {
        val codec = jp.codec
        val node = codec.readTree<JsonNode>(jp)
        return when (node["type"].asText()) {
            "ach" -> codec.treeToValue(node, AchDetails::class.java)
            "dotpay" -> codec.treeToValue(node, DotpayDetails::class.java)
            "giropay" -> codec.treeToValue(node, GiropayDetails::class.java)
            "klarna", "klarna_paynow", "klarna_account" -> codec.treeToValue(node, KlarnaDetails::class.java)
            else -> codec.treeToValue(node, DefaultPaymentMethodDetails::class.java)
        }
    }
}
