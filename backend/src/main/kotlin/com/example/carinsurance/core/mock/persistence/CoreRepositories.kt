package com.example.carinsurance.core.mock.persistence

import java.time.Instant
import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.transaction.annotation.Transactional

interface CustomerRepository : CrudRepository<CustomerEntity, Long> {
    fun findByCustomerNumber(customerNumber: String): CustomerEntity?
}

interface InsuranceAgreementRepository : CrudRepository<InsuranceAgreementEntity, Long> {
    fun findByAgreementNumber(agreementNumber: String): InsuranceAgreementEntity?

    // Mock
    @Transactional
    @Modifying
    @Query(
        "UPDATE insurance_agreement SET status = 'SENT', updated_at = :updatedAt WHERE agreement_number = :agreementNumber"
    )
    fun markAsSent(agreementNumber: String, updatedAt: Instant): Int
}
