package com.example.carinsurance.core.mock

import com.example.carinsurance.core.AgreementDetails
import com.example.carinsurance.core.AgreementResult
import com.example.carinsurance.core.AgreementStatus
import com.example.carinsurance.core.CoreSystemClient
import com.example.carinsurance.core.CustomerDetails
import com.example.carinsurance.core.CustomerResult
import java.util.UUID

class InMemoryCoreSystemClient : CoreSystemClient {
    private val customers = mutableMapOf<String, CustomerDetails>()
    private val agreements = mutableMapOf<String, StoredAgreement>()

    @Synchronized
    override fun createCustomer(customer: CustomerDetails): CustomerResult {
        val customerNumber = "KUN-${UUID.randomUUID()}"
        customers[customerNumber] = customer
        return CustomerResult(customerNumber)
    }

    @Synchronized
    override fun createAgreement(agreement: AgreementDetails): AgreementResult {
        require(customers.containsKey(agreement.customerNumber)) { "Kunde eksisterer ikke" }
        val agreementNumber = "AVT-${UUID.randomUUID()}"
        val result = AgreementResult(agreementNumber, AgreementStatus.CREATED)
        agreements[agreementNumber] = StoredAgreement(agreement, result)
        return result
    }

    @Synchronized
    override fun markAgreementAsSent(agreementNumber: String): AgreementResult {
        val stored = requireNotNull(agreements[agreementNumber]) { "Avtale eksisterer ikke" }
        val result = stored.result.copy(status = AgreementStatus.SENT)
        agreements[agreementNumber] = StoredAgreement(stored.details, result)
        return result
    }

    private class StoredAgreement(val details: AgreementDetails, val result: AgreementResult)
}
