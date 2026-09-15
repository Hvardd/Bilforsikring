package com.example.carinsurance.api

data class ErrorResponse(
    val code: ErrorCode,
    val message: String,
    val correlationId: String,
    val fieldErrors: Map<String, String> = emptyMap(),
)

enum class ErrorCode {
    VALIDATION_ERROR,
    REQUEST_ERROR,
    CUSTOMER_CREATION_FAILED,
    AGREEMENT_CREATION_FAILED,
    LETTER_SENDING_FAILED,
    AGREEMENT_STATUS_UPDATE_FAILED,
    INTERNAL_ERROR,
}
