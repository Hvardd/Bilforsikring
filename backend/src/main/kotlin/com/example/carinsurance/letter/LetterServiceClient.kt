package com.example.carinsurance.letter

interface LetterServiceClient {
    fun sendAgreement(letter: AgreementLetter): LetterResult
}
