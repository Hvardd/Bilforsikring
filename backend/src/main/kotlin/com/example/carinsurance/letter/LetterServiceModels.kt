package com.example.carinsurance.letter

class AgreementLetter(val agreementNumber: String, val recipientEmail: String)

data class LetterResult(val status: LetterStatus)

enum class LetterStatus {
    SENT
}
