package io.techyowls.logging.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * Adds tracing context to every request using MDC (Mapped Diagnostic Context).
 *
 * MDC automatically adds these fields to every log statement within the request:
 * - requestId: Unique ID for this request
 * - clientIp: Client IP address
 * - userAgent: Browser/client info
 * - userId: If authenticated
 *
 * This enables searching logs by requestId across all services.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class RequestTracingFilter extends OncePerRequestFilter {

    public static final String REQUEST_ID = "requestId";
    public static final String USER_ID = "userId";
    public static final String CLIENT_IP = "clientIp";
    public static final String REQUEST_URI = "requestUri";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                     HttpServletResponse response,
                                     FilterChain filterChain)
            throws ServletException, IOException {

        // Get or generate request ID (for distributed tracing)
        String requestId = request.getHeader("X-Request-ID");
        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString().substring(0, 8);
        }

        // Get client IP (handle proxies)
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }

        // Add to MDC - these fields appear in EVERY log statement
        MDC.put(REQUEST_ID, requestId);
        MDC.put(CLIENT_IP, clientIp);
        MDC.put(REQUEST_URI, request.getRequestURI());

        // Add request ID to response for client-side correlation
        response.setHeader("X-Request-ID", requestId);

        long startTime = System.currentTimeMillis();

        try {
            log.info("Request started: {} {}", request.getMethod(), request.getRequestURI());
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - startTime;

            log.info("Request completed: {} {} - {} in {}ms",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duration
            );

            // CRITICAL: Clear MDC after request to prevent memory leaks
            MDC.clear();
        }
    }
}
