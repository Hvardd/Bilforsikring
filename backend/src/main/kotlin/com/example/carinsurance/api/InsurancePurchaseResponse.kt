package com.example.carinsurance.api

data class InsurancePurchaseResponse(val agreementNumber: String, val status: PurchaseStatus)

enum class PurchaseStatus {
    SENT
}
