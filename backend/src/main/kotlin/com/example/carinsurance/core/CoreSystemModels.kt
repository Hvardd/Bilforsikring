package com.example.carinsurance.core

class CustomerDetails(
    val nationalIdentityNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
)

class AgreementDetails(
    val customerNumber: String,
    val registrationNumber: String,
    val bonus: Int,
    val insuranceType: InsuranceType,
)

data class CustomerResult(val customerNumber: String)

data class AgreementResult(val agreementNumber: String, val status: AgreementStatus)

enum class AgreementStatus {
    CREATED,
    SENT,
}
