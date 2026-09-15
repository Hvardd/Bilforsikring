package com.example.carinsurance.purchase

import com.example.carinsurance.api.ErrorCode
import com.example.carinsurance.api.InsurancePurchaseRequest
import com.example.carinsurance.api.InsurancePurchaseResponse
import com.example.carinsurance.api.PurchaseStatus
import com.example.carinsurance.core.AgreementDetails
import com.example.carinsurance.core.AgreementStatus
import com.example.carinsurance.core.CoreSystemClient
import com.example.carinsurance.core.CustomerDetails
import com.example.carinsurance.letter.AgreementLetter
import com.example.carinsurance.letter.LetterServiceClient
import com.example.carinsurance.letter.LetterStatus
import org.springframework.stereotype.Service

@Service
class InsurancePurchaseService(
    private val coreSystem: CoreSystemClient,
    private val letterService: LetterServiceClient,
) {
    fun purchase(request: InsurancePurchaseRequest): InsurancePurchaseResponse {
        val customerDetails =
            CustomerDetails(
                requireNotNull(request.nationalIdentityNumber),
                requireNotNull(request.firstName),
                requireNotNull(request.lastName),
                requireNotNull(request.email),
            )
        val registrationNumber = requireNotNull(request.registrationNumber)
        val bonus = requireNotNull(request.bonus)
        val insuranceType = requireNotNull(request.insuranceType)

        val customer =
            try {
                coreSystem.createCustomer(customerDetails)
            } catch (exception: Exception) {
                throw PurchaseFailedException(
                    ErrorCode.CUSTOMER_CREATION_FAILED,
                    "Kunne ikke opprette kunde",
                    exception,
                )
            }

        val agreement =
            try {
                coreSystem.createAgreement(
                    AgreementDetails(
                        customer.customerNumber,
                        registrationNumber,
                        bonus,
                        insuranceType,
                    )
                )
            } catch (exception: Exception) {
                throw PurchaseFailedException(
                    ErrorCode.AGREEMENT_CREATION_FAILED,
                    "Kunne ikke opprette avtale",
                    exception,
                )
            }

        try {
            val letter =
                letterService.sendAgreement(
                    AgreementLetter(agreement.agreementNumber, customerDetails.email)
                )
            check(letter.status == LetterStatus.SENT) {
                "Brevtjenesten bekreftet ikke at avtalen ble sendt"
            }
        } catch (exception: Exception) {
            throw PurchaseFailedException(
                ErrorCode.LETTER_SENDING_FAILED,
                "Kunne ikke sende avtalen",
                exception,
            )
        }

        val sentAgreement =
            try {
                val result = coreSystem.markAgreementAsSent(agreement.agreementNumber)
                check(
                    result.agreementNumber == agreement.agreementNumber &&
                        result.status == AgreementStatus.SENT
                ) {
                    "Fagsystemet bekreftet ikke at avtalen var sendt"
                }
                result
            } catch (exception: Exception) {
                throw PurchaseFailedException(
                    ErrorCode.AGREEMENT_STATUS_UPDATE_FAILED,
                    "Kunne ikke oppdatere avtalestatus",
                    exception,
                )
            }

        return InsurancePurchaseResponse(sentAgreement.agreementNumber, PurchaseStatus.SENT)
    }
}
