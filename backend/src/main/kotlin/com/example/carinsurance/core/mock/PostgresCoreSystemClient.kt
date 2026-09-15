package com.example.carinsurance.core.mock

import com.example.carinsurance.core.AgreementDetails
import com.example.carinsurance.core.AgreementResult
import com.example.carinsurance.core.AgreementStatus
import com.example.carinsurance.core.CoreSystemClient
import com.example.carinsurance.core.CustomerDetails
import com.example.carinsurance.core.CustomerResult
import com.example.carinsurance.core.mock.persistence.CustomerEntity
import com.example.carinsurance.core.mock.persistence.CustomerRepository
import com.example.carinsurance.core.mock.persistence.InsuranceAgreementEntity
import com.example.carinsurance.core.mock.persistence.InsuranceAgreementRepository
import java.time.Instant
import java.util.UUID
import org.springframework.stereotype.Component

@Component
class PostgresCoreSystemClient(
    private val customers: CustomerRepository,
    private val agreements: InsuranceAgreementRepository,
) : CoreSystemClient {
    override fun createCustomer(customer: CustomerDetails): CustomerResult {
        val stored =
            customers.save(
                CustomerEntity(
                    customerNumber = "KUN-${UUID.randomUUID()}",
                    nationalIdentityNumber = customer.nationalIdentityNumber,
                    firstName = customer.firstName,
                    lastName = customer.lastName,
                    email = customer.email,
                    createdAt = Instant.now(),
                )
            )
        return CustomerResult(stored.customerNumber)
    }

    override fun createAgreement(agreement: AgreementDetails): AgreementResult {
        val customer =
            requireNotNull(customers.findByCustomerNumber(agreement.customerNumber)) {
                "Kunde eksisterer ikke"
            }
        val now = Instant.now()
        val stored =
            agreements.save(
                InsuranceAgreementEntity(
                    agreementNumber = "AVT-${UUID.randomUUID()}",
                    customerId = requireNotNull(customer.id),
                    registrationNumber = agreement.registrationNumber,
                    bonus = agreement.bonus,
                    insuranceType = agreement.insuranceType,
                    status = AgreementStatus.CREATED,
                    createdAt = now,
                    updatedAt = now,
                )
            )
        return AgreementResult(stored.agreementNumber, stored.status)
    }

    override fun markAgreementAsSent(agreementNumber: String): AgreementResult {
        require(agreements.markAsSent(agreementNumber, Instant.now()) == 1) {
            "Avtale eksisterer ikke"
        }
        return AgreementResult(agreementNumber, AgreementStatus.SENT)
    }
}
