package com.example.carinsurance.api

import com.example.carinsurance.core.mock.InMemoryCoreSystemClient
import com.example.carinsurance.letter.mock.MockLetterServiceClient
import com.example.carinsurance.purchase.InsurancePurchaseService
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder

class InsurancePurchaseControllerTests {
    private val mvc =
        MockMvcBuilders.standaloneSetup(
                InsurancePurchaseController(
                    InsurancePurchaseService(InMemoryCoreSystemClient(), MockLetterServiceClient())
                )
            )
            .setControllerAdvice(ApiExceptionHandler())
            .addFilters<StandaloneMockMvcBuilder>(CorrelationIdFilter())
            .build()

    @Test
    fun `gyldig kjøp returnerer avtalenummer og sendt status`() {
        mvc.perform(
                post("/api/insurance-purchases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Correlation-ID", "purchase-test-id")
                    .content(
                        """
                    {
                        "registrationNumber": "AB12345",
                        "bonus": 60,
                        "insuranceType": "KASKO",
                        "nationalIdentityNumber": "01019012345",
                        "firstName": "Ola",
                        "lastName": "Nordmann",
                        "email": "ola@example.no"
                    }
                    """
                            .trimIndent()
                    )
            )
            .andExpect(status().isOk)
            .andExpect(header().string("X-Correlation-ID", "purchase-test-id"))
            .andExpect(jsonPath("$.agreementNumber").isNotEmpty)
            .andExpect(jsonPath("$.status").value("SENT"))
    }

    @Test
    fun `ugyldig kjøp avvises av valideringen`() {
        mvc.perform(
                post("/api/insurance-purchases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{}")
            )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.fieldErrors.email").value("Epost kreves"))
            .andExpect(header().exists("X-Correlation-ID"))
    }

    @Test
    fun `ugyldig JSON returnerer en kontrollert feilrespons`() {
        mvc.perform(
                post("/api/insurance-purchases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("X-Correlation-ID", "malformed-test-id")
                    .content("""{"nationalIdentityNumber":"01019012345", broken}""")
            )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
            .andExpect(jsonPath("$.message").value("Forespørselen mangler eller er ugyldig"))
            .andExpect(jsonPath("$.correlationId").value("malformed-test-id"))
            .andExpect(header().string("X-Correlation-ID", "malformed-test-id"))
    }

    @Test
    fun `forsikringstype kreves og bare kjente typer godtas`() {
        val base =
            """{"registrationNumber":"AB12345","bonus":60,
            "nationalIdentityNumber":"01019012345","firstName":"Ola",
            "lastName":"Nordmann","email":"ola@example.no"}"""
        for (type in listOf("ANSVAR", "DELKASKO", "KASKO", "TOPPKASKO")) {
            mvc.perform(
                    post("/api/insurance-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base.dropLast(1) + ",\"insuranceType\":\"$type\"}")
                )
                .andExpect(status().isOk)
        }
        mvc.perform(
                post("/api/insurance-purchases")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(base)
            )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.fieldErrors.insuranceType").value("Forsikringstype må velges"))
        for (type in listOf("null", "\"UNKNOWN\"", "99")) {
            mvc.perform(
                    post("/api/insurance-purchases")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(base.dropLast(1) + ",\"insuranceType\":$type}")
                )
                .andExpect(status().isBadRequest)
        }
    }
}
