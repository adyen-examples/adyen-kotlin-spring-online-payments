package com.adyen.checkout

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import java.lang.System.clearProperty
import java.lang.System.setProperty

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class OnlinePaymentsApplicationTests {

    @BeforeAll
    internal fun onceExecutedBeforeAll() {
        setProperty("ADYEN_API_KEY", "testKey");
        setProperty("ADYEN_MERCHANT_ACCOUNT", "testAccount");
        setProperty("ADYEN_CLIENT_KEY", "testKey");
    }

    @AfterAll
    internal fun onceExecutedAfterAll(){
        clearProperty("ADYEN_API_KEY");
        clearProperty("ADYEN_MERCHANT_ACCOUNT");
        clearProperty("ADYEN_CLIENT_KEY");
    }

	@Test
	fun contextLoads() {
	}

}
