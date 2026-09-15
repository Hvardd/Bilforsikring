package com.example.carinsurance.core

import com.example.carinsurance.core.mock.persistence.CustomerRepository
import com.example.carinsurance.core.mock.persistence.InsuranceAgreementRepository
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@EnabledIfEnvironmentVariable(named = "TEST_DATABASE_URL", matches = ".+")
@Transactional
class PostgresCoreSystemClientIntegrationTests {

    @Autowired private lateinit var client: CoreSystemClient
    @Autowired private lateinit var customers: CustomerRepository
    @Autowired private lateinit var agreements: InsuranceAgreementRepository
    @Autowired private lateinit var jdbc: JdbcTemplate

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun database(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") { System.getenv("TEST_DATABASE_URL") }
            registry.add("spring.datasource.username") {
                System.getenv("TEST_DATABASE_USERNAME") ?: "car_insurance"
            }
            registry.add("spring.datasource.password") {
                System.getenv("TEST_DATABASE_PASSWORD") ?: ""
            }
        }
    }

    @Test
    fun `kunde og avtale lagres og status kan oppdateres`() {
        val customer =
            client.createCustomer(
                CustomerDetails("01019012345", "Ola", "Nordmann", "ola@example.no")
            )

        val storedCustomer = assertNotNull(customers.findByCustomerNumber(customer.customerNumber))

        assertEquals("Ola", storedCustomer.firstName)

        val agreement =
            client.createAgreement(
                AgreementDetails(customer.customerNumber, "AB12345", 60, InsuranceType.KASKO)
            )

        val storedAgreement =
            assertNotNull(agreements.findByAgreementNumber(agreement.agreementNumber))

        assertEquals(AgreementStatus.CREATED, storedAgreement.status)

        client.markAgreementAsSent(agreement.agreementNumber)

        val updatedAgreement =
            assertNotNull(agreements.findByAgreementNumber(agreement.agreementNumber))

        assertEquals(AgreementStatus.SENT, updatedAgreement.status)
    }

    @Test
    fun `ukjente referanser avvises`() {
        assertFailsWith<IllegalArgumentException> {
            client.createAgreement(AgreementDetails("missing", "AB12345", 60, InsuranceType.ANSVAR))
        }

        assertFailsWith<IllegalArgumentException> { client.markAgreementAsSent("missing") }
    }

    @Test
    fun `databasen krever at avtalen tilhører en eksisterende kunde`() {
        assertFailsWith<DataIntegrityViolationException> {
            jdbc.update(
                """
                INSERT INTO insurance_agreement
                    (agreement_number, customer_id, registration_number, bonus, status, insurance_type)
                VALUES
                    ('AVT-orphan', -1, 'AB12345', 60, 'CREATED', 'ANSVAR')
                """
                    .trimIndent()
            )
        }
    }

    @Test
    fun `alle forsikringstyper lagres og bevares ved statusoppdatering`() {
        val customer =
            client.createCustomer(
                CustomerDetails("01019012345", "Ola", "Nordmann", "ola@example.no")
            )
        for (type in InsuranceType.entries) {
            val agreement =
                client.createAgreement(
                    AgreementDetails(customer.customerNumber, "AB12345", 60, type)
                )
            assertEquals(
                type,
                assertNotNull(agreements.findByAgreementNumber(agreement.agreementNumber))
                    .insuranceType,
            )
            client.markAgreementAsSent(agreement.agreementNumber)
            assertEquals(
                type,
                assertNotNull(agreements.findByAgreementNumber(agreement.agreementNumber))
                    .insuranceType,
            )
        }
    }

    @Test
    fun `databasen avviser ukjent forsikringstype`() {
        val customer =
            client.createCustomer(
                CustomerDetails("01019012345", "Ola", "Nordmann", "ola@example.no")
            )
        val agreement =
            client.createAgreement(
                AgreementDetails(customer.customerNumber, "AB12345", 60, InsuranceType.ANSVAR)
            )
        assertFailsWith<DataIntegrityViolationException> {
            jdbc.update(
                "UPDATE insurance_agreement SET insurance_type = ? WHERE agreement_number = ?",
                "UNKNOWN",
                agreement.agreementNumber,
            )
        }
    }
}
