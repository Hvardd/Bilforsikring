package com.example.carinsurance.core

interface CoreSystemClient {
    fun createCustomer(customer: CustomerDetails): CustomerResult

    fun createAgreement(agreement: AgreementDetails): AgreementResult

    fun markAgreementAsSent(agreementNumber: String): AgreementResult
}
