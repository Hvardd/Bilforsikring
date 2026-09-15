package com.example.carinsurance.purchase

import com.example.carinsurance.api.*
import com.example.carinsurance.core.*
import com.example.carinsurance.letter.*
import kotlin.test.*
import org.junit.jupiter.api.Test

class InsurancePurchaseServiceTests {

    private val request =
        InsurancePurchaseRequest(
            " ab12345 ",
            60,
            "01019012345",
            "Ola",
            "Nordmann",
            "ola@example.no",
            InsuranceType.TOPPKASKO,
        )

    private class Clients(
        private val failingStep: String? = null,
        private val confirmSent: Boolean = true,
    ) {
        val calls = mutableListOf<String>()
        val failure = IllegalStateException("Simulert ekstern feil")

        private fun record(step: String) {
            calls += step

            if (step == failingStep) {
                throw failure
            }
        }

        val core =
            object : CoreSystemClient {

                override fun createCustomer(customer: CustomerDetails): CustomerResult {
                    record("customer")

                    assertEquals("01019012345", customer.nationalIdentityNumber)
                    assertEquals("Ola", customer.firstName)
                    assertEquals("Nordmann", customer.lastName)
                    assertEquals("ola@example.no", customer.email)

                    return CustomerResult("KUN-123")
                }

                override fun createAgreement(agreement: AgreementDetails): AgreementResult {
                    record("agreement")

                    assertEquals("KUN-123", agreement.customerNumber)
                    assertEquals("AB12345", agreement.registrationNumber)
                    assertEquals(60, agreement.bonus)
                    assertEquals(InsuranceType.TOPPKASKO, agreement.insuranceType)

                    return AgreementResult("AVT-456", AgreementStatus.CREATED)
                }

                override fun markAgreementAsSent(agreementNumber: String): AgreementResult {
                    record("status")

                    assertEquals("AVT-456", agreementNumber)

                    return AgreementResult(
                        agreementNumber,
                        if (confirmSent) AgreementStatus.SENT else AgreementStatus.CREATED,
                    )
                }
            }

        val letter =
            object : LetterServiceClient {

                override fun sendAgreement(letter: AgreementLetter): LetterResult {
                    record("letter")

                    assertEquals("AVT-456", letter.agreementNumber)
                    assertEquals("ola@example.no", letter.recipientEmail)

                    return LetterResult(LetterStatus.SENT)
                }
            }
    }

    @Test
    fun `kjøp sender data gjennom tjenestene i riktig rekkefølge og returnerer bekreftet status`() {
        val clients = Clients()

        val response = InsurancePurchaseService(clients.core, clients.letter).purchase(request)

        assertEquals(listOf("customer", "agreement", "letter", "status"), clients.calls)

        assertEquals(InsurancePurchaseResponse("AVT-456", PurchaseStatus.SENT), response)
    }

    @Test
    fun `feil identifiserer steget og stopper etterfølgende kall`() {
        val steps = listOf("customer", "agreement", "letter", "status")

        val codes =
            listOf(
                ErrorCode.CUSTOMER_CREATION_FAILED,
                ErrorCode.AGREEMENT_CREATION_FAILED,
                ErrorCode.LETTER_SENDING_FAILED,
                ErrorCode.AGREEMENT_STATUS_UPDATE_FAILED,
            )

        steps.forEachIndexed { index, step ->
            val clients = Clients(failingStep = step)

            val failure =
                assertFailsWith<PurchaseFailedException> {
                    InsurancePurchaseService(clients.core, clients.letter).purchase(request)
                }

            assertEquals(codes[index], failure.code)
            assertSame(clients.failure, failure.cause)
            assertEquals(steps.take(index + 1), clients.calls)
        }
    }

    @Test
    fun `kjøp returnerer ikke suksess når fagsystemet ikke bekrefter sendt status`() {
        val clients = Clients(confirmSent = false)

        val failure =
            assertFailsWith<PurchaseFailedException> {
                InsurancePurchaseService(clients.core, clients.letter).purchase(request)
            }

        assertEquals(ErrorCode.AGREEMENT_STATUS_UPDATE_FAILED, failure.code)
    }
}
