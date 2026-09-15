package com.example.carinsurance.api

import com.example.carinsurance.purchase.PurchaseFailedException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> {
        val fields =
            ex.bindingResult.fieldErrors
                .sortedWith(
                    compareBy({ it.field }, { it.code != "NotBlank" && it.code != "NotNull" })
                )
                .groupBy { it.field }
                .mapValues { (_, errors) -> errors.first().defaultMessage ?: "Ugyldig verdi" }

        return response(
            HttpStatus.BAD_REQUEST,
            ErrorCode.VALIDATION_ERROR,
            "Validering av forespørselen feilet",
            servletRequest(request),
            fields,
            headers,
        )
    }

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        statusCode: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any> =
        response(
            statusCode,
            if (statusCode.value() == 400) {
                ErrorCode.VALIDATION_ERROR
            } else {
                ErrorCode.REQUEST_ERROR
            },
            if (statusCode.value() == 400) {
                "Forespørselen mangler eller er ugyldig"
            } else {
                "Forespørselen kunne ikke behandles"
            },
            servletRequest(request),
            headers = headers,
        )

    @ExceptionHandler(PurchaseFailedException::class)
    fun purchaseFailed(
        ex: PurchaseFailedException,
        request: HttpServletRequest,
    ): ResponseEntity<Any> {
        val message =
            when (ex.code) {
                ErrorCode.CUSTOMER_CREATION_FAILED -> "Kunne ikke opprette kunde"

                ErrorCode.AGREEMENT_CREATION_FAILED -> "Kunne ikke opprette avtale"

                ErrorCode.LETTER_SENDING_FAILED -> "Kunne ikke sende avtalen"

                ErrorCode.AGREEMENT_STATUS_UPDATE_FAILED -> "Kunne ikke oppdatere avtalestatus"

                else -> "Kjøpet kunne ikke gjennomføres"
            }

        return response(HttpStatus.BAD_GATEWAY, ex.code, message, request)
    }

    @ExceptionHandler(Exception::class)
    fun unexpectedFailure(ex: Exception, request: HttpServletRequest): ResponseEntity<Any> =
        response(
            HttpStatus.INTERNAL_SERVER_ERROR,
            ErrorCode.INTERNAL_ERROR,
            "En uventet feil oppstod",
            request,
        )

    private fun response(
        status: HttpStatusCode,
        code: ErrorCode,
        message: String,
        request: HttpServletRequest,
        fields: Map<String, String> = emptyMap(),
        headers: HttpHeaders = HttpHeaders(),
    ): ResponseEntity<Any> {
        val correlationId = CorrelationIdFilter.correlationId(request)

        val responseHeaders = HttpHeaders()
        responseHeaders.putAll(headers)
        responseHeaders.set(CorrelationIdFilter.HEADER, correlationId)

        return ResponseEntity(
            ErrorResponse(code, message, correlationId, fields),
            responseHeaders,
            status,
        )
    }

    private fun servletRequest(request: WebRequest): HttpServletRequest =
        (request as ServletWebRequest).request
}
