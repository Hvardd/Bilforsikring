package com.example.carinsurance.api

import com.example.carinsurance.core.InsuranceType
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import java.util.Locale

class InsurancePurchaseRequest(
    registrationNumber: String? = null,
    @field:NotNull(message = "Bonus verdi kreves")
    @field:Min(value = 0, message = "Bonus må være mellom 0 og 80")
    @field:Max(value = 80, message = "Bonus må være mellom 0 og 80")
    val bonus: Int? = null,
    @field:NotBlank(message = "Personnummer kreves")
    @field:Pattern(regexp = "[0-9]{11}", message = "Personnummer må inneholde 11 siffer")
    val nationalIdentityNumber: String? = null,
    @field:NotBlank(message = "Fornavn kreves") val firstName: String? = null,
    @field:NotBlank(message = "Etternavn kreves") val lastName: String? = null,
    @field:NotBlank(message = "Epost kreves")
    @field:Email(message = "Ugyldig e-postadresse")
    val email: String? = null,
    @field:NotNull(message = "Forsikringstype må velges") val insuranceType: InsuranceType? = null,
) {
    @field:NotBlank(message = "Registreringsnummer kreves")
    val registrationNumber: String? =
        registrationNumber?.filterNot { it.isWhitespace() }?.uppercase(Locale.ROOT)
}
