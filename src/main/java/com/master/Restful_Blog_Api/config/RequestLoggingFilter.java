package com.master.Restful_Blog_Api.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Order(1)
@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    // key name used in MDC must match %X{requestId} in logback-spring.xml
    private static final String REQUEST_ID_KEY = "requestId";

    // Header name: client can send their own trace ID (useful in API gateways)
    private static final String REQUEST_ID_HEADER= "X-Request-ID";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        /// -> (Get) If we use API gateway already sent an X-Request-ID header, reuse it and
        ///  trace a request across multiple services
        //// OR (Generate) request ID
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if(requestId == null || requestId.isBlank()) {
            // 8-4-4-4-12
            // 32 hex digits + 4 hyphens = 36 chars
            // take first 8 e.g. a3f9b2c1
            requestId = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        }
        /// -> Put ID into MDC - every log line will carry it automatically
        /// It stores "a3f9b2c1" in a thread-local map.
        /// Now every single log statement on this thread automatically includes it — no one has to pass it around manually.
        MDC.put(REQUEST_ID_KEY, requestId);

        /// -> Also put it in response header so client can reference it
        /// useful when use reports "I got an error" they give ID
        response.setHeader(REQUEST_ID_HEADER, requestId);

        /// -> log incoming request
        log.debug("-> {} {} | ip={}", request.getMethod(), request.getRequestURI(), getClientIp(request));

        long startTime = System.currentTimeMillis();

        try {
            // let rest of filter chain run (other filters, controllers, etc.)
            filterChain.doFilter(request, response);
        }
        finally {
            long duration = System.currentTimeMillis()- startTime;
            log.debug("<- {} {} | status={} | {}ms",
                    request.getMethod(), request.getRequestURI(),
                    response.getStatus(), duration);

            /// important line - because if you didn't clear MDC after request, next request have the same Correlation ID
            MDC.clear();
        }
    }

    /** Extracts real client IP
     * when behind a reverse proxy (Nginx, AWS ALB), real IP is in X-Forwarded-For
     * or Falls back to request.getRemoteAddr() for direct connections**/
    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if(forwarded != null && !forwarded.isBlank()) {
            // If you go across multiple proxies X-Forwarded-For: 203.0.113.42, 70.41.3.18, 10.0.0.5
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
