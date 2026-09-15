package com.example.carinsurance.letter.mock

import com.example.carinsurance.letter.AgreementLetter
import com.example.carinsurance.letter.LetterResult
import com.example.carinsurance.letter.LetterServiceClient
import com.example.carinsurance.letter.LetterStatus
import org.springframework.stereotype.Component

@Component
class MockLetterServiceClient : LetterServiceClient {
    override fun sendAgreement(letter: AgreementLetter): LetterResult =
        LetterResult(LetterStatus.SENT)
}
