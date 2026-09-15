package com.example.carinsurance.api

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.util.UUID
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class CorrelationIdFilter : OncePerRequestFilter() {
    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ) {
        response.setHeader(HEADER, correlationId(request))
        chain.doFilter(request, response)
    }

    override fun shouldNotFilterErrorDispatch(): Boolean = false

    companion object {
        const val HEADER = "X-Correlation-ID"
        private const val ATTRIBUTE = "carinsurance.correlationId"

        fun correlationId(request: HttpServletRequest): String {
            val existing = request.getAttribute(ATTRIBUTE) as? String
            if (existing != null) return existing
            val id =
                request.getHeader(HEADER)?.takeIf { it.isNotBlank() }
                    ?: UUID.randomUUID().toString()
            request.setAttribute(ATTRIBUTE, id)
            return id
        }
    }
}
