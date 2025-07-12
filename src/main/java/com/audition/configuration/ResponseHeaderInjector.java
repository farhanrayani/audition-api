package com.audition.configuration;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;

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

            // Get current span context
            Span currentSpan = Span.current();
            SpanContext spanContext = currentSpan.getSpanContext();

            // Inject trace and span IDs into response headers
            if (spanContext.isValid()) {
                httpResponse.setHeader(TRACE_ID_HEADER, spanContext.getTraceId());
                httpResponse.setHeader(SPAN_ID_HEADER, spanContext.getSpanId());
            }
        }

        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
        // No cleanup needed
    }
}