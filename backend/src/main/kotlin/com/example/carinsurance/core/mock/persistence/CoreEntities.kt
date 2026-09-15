package com.example.carinsurance.core.mock.persistence

import com.example.carinsurance.core.AgreementStatus
import com.example.carinsurance.core.InsuranceType
import java.time.Instant
import org.springframework.data.annotation.Id
import org.springframework.data.relational.core.mapping.Table

@Table("customer")
class CustomerEntity(
    @Id val id: Long? = null,
    val customerNumber: String,
    val nationalIdentityNumber: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val createdAt: Instant,
)

@Table("insurance_agreement")
class InsuranceAgreementEntity(
    @Id val id: Long? = null,
    val agreementNumber: String,
    val customerId: Long,
    val registrationNumber: String,
    val bonus: Int,
    val insuranceType: InsuranceType,
    val status: AgreementStatus,
    val createdAt: Instant,
    val updatedAt: Instant,
)
