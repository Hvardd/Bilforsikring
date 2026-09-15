package com.example.carinsurance

import com.example.carinsurance.core.CoreSystemClient
import com.example.carinsurance.letter.LetterServiceClient
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest(
    properties =
        [
            "spring.autoconfigure.exclude=org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration,org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration"
        ]
)
class CarInsuranceApplicationTests {

    @MockitoBean private lateinit var coreSystemClient: CoreSystemClient

    @Autowired private lateinit var letterServiceClient: LetterServiceClient

    @Test
    fun `applikasjonskonteksten starter`() {
        assertNotNull(letterServiceClient)
    }
}
