package com.example.carinsurance.purchase

import com.example.carinsurance.api.ErrorCode

class PurchaseFailedException(val code: ErrorCode, message: String, cause: Exception) :
    RuntimeException(message, cause)
