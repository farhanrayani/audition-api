package com.audition.configuration;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Servlet filter for injecting OpenTelemetry trace and span IDs into HTTP response headers.
 *
 * This filter automatically adds the following headers to all HTTP responses:
 * - X-Trace-Id - Distributed tracing trace identifier
 * - X-Span-Id - Current span identifier
 *
 * Headers are only added if the corresponding values are available in the
 * Mapped Diagnostic Context (MDC). This enables client-side correlation of
 * requests with server-side logs and traces.
 *
 * @author Farhan Rayani
 */

@Component
public class ResponseHeaderInjector implements Filter {

    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String SPAN_ID_HEADER = "X-Span-Id";

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        // No initialization needed
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Get trace and span IDs from MDC (Micrometer Tracing)
            String traceId = MDC.get("traceId");
            String spanId = MDC.get("spanId");

            // Inject trace and span IDs into response headers if available
            if (traceId != null && !traceId.isEmpty()) {
                httpResponse.setHeader(TRACE_ID_HEADER, traceId);
            }

            if (spanId != null && !spanId.isEmpty()) {
                httpResponse.setHeader(SPAN_ID_HEADER, spanId);
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}