package com.example.carinsurance.api

import com.example.carinsurance.purchase.InsurancePurchaseService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/insurance-purchases")
class InsurancePurchaseController(private val purchaseService: InsurancePurchaseService) {
    @PostMapping
    fun purchase(@Valid @RequestBody request: InsurancePurchaseRequest): InsurancePurchaseResponse =
        purchaseService.purchase(request)
}
