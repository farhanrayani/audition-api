package com.audition;

import com.audition.common.logging.AuditionLogger;
import com.audition.configuration.ResponseHeaderInjector;
import com.audition.configuration.WebServiceConfiguration;
import com.audition.configuration.CacheConfiguration;
import com.audition.web.advice.ExceptionControllerAdvice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.http.HttpStatus;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Final tests to complete coverage of remaining uncovered methods and edge cases
 */
@ExtendWith(MockitoExtension.class)
class CoverageCompletionTest {

    @Mock
    private Logger mockLogger;

    @Mock
    private ServletRequest mockRequest;

    @Mock
    private HttpServletResponse mockHttpResponse;

    @Mock
    private ServletResponse mockResponse;

    @Mock
    private FilterChain mockFilterChain;

    private AuditionLogger auditionLogger;
    private ResponseHeaderInjector headerInjector;
    private WebServiceConfiguration webConfig;
    private CacheConfiguration cacheConfig;
    private ExceptionControllerAdvice exceptionAdvice;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
        headerInjector = new ResponseHeaderInjector();
        webConfig = new WebServiceConfiguration();
        cacheConfig = new CacheConfiguration();
        exceptionAdvice = new ExceptionControllerAdvice();

        // Inject the mock logger into exception advice using reflection
        try {
            java.lang.reflect.Field field = ExceptionControllerAdvice.class.getDeclaredField("logger");
            field.setAccessible(true);
            field.set(exceptionAdvice, auditionLogger);
        } catch (Exception e) {
            // Continue without injection if field doesn't exist
        }
    }

    @Test
    void testResponseHeaderInjectorWithTraceIds() throws Exception {
        // Given - Set up MDC with trace and span IDs
        MDC.put("traceId", "test-trace-id-123");
        MDC.put("spanId", "test-span-id-456");

        try {
            // When
            headerInjector.doFilter(mockRequest, mockHttpResponse, mockFilterChain);

            // Then
            verify(mockHttpResponse).setHeader("X-Trace-Id", "test-trace-id-123");
            verify(mockHttpResponse).setHeader("X-Span-Id", "test-span-id-456");
            verify(mockFilterChain).doFilter(mockRequest, mockHttpResponse);
        } finally {
            // Clean up MDC
            MDC.clear();
        }
    }

    @Test
    void testResponseHeaderInjectorWithEmptyTraceIds() throws Exception {
        // Given - Set up MDC with empty trace and span IDs
        MDC.put("traceId", "");
        MDC.put("spanId", "");

        try {
            // When
            headerInjector.doFilter(mockRequest, mockHttpResponse, mockFilterChain);

            // Then - empty strings should not set headers
            verify(mockHttpResponse, never()).setHeader(eq("X-Trace-Id"), anyString());
            verify(mockHttpResponse, never()).setHeader(eq("X-Span-Id"), anyString());
            verify(mockFilterChain).doFilter(mockRequest, mockHttpResponse);
        } finally {
            MDC.clear();
        }
    }

    @Test
    void testResponseHeaderInjectorWithNullTraceIds() throws Exception {
        // Given - MDC is empty (null values)
        MDC.clear();

        // When
        headerInjector.doFilter(mockRequest, mockHttpResponse, mockFilterChain);

        // Then - null values should not set headers
        verify(mockHttpResponse, never()).setHeader(eq("X-Trace-Id"), anyString());
        verify(mockHttpResponse, never()).setHeader(eq("X-Span-Id"), anyString());
        verify(mockFilterChain).doFilter(mockRequest, mockHttpResponse);
    }

    @Test
    void testResponseHeaderInjectorWithNonHttpResponse() throws Exception {
        // When
        headerInjector.doFilter(mockRequest, mockResponse, mockFilterChain);

        // Then - should not attempt to set headers on non-HTTP response
        verify(mockFilterChain).doFilter(mockRequest, mockResponse);
        verifyNoInteractions(mockHttpResponse);
    }

    @Test
    void testAuditionLoggerWarnWithThreeParameters() {
        // Test the warn method that takes three string parameters
        when(mockLogger.isWarnEnabled()).thenReturn(true);

        // When
        auditionLogger.warn(mockLogger, "Test message", "postId123", "Additional message");

        // Then - this method currently has an empty implementation
        verify(mockLogger, never()).warn(anyString());
        verify(mockLogger, never()).warn(anyString(), any(), any());
    }

    @Test
    void testProblemDetailMessageCreation() {
        // Test the private method createStandardProblemDetailMessage via public method
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Test Title");
        problemDetail.setDetail("Test Detail");
        problemDetail.setInstance(URI.create("/test-instance"));

        when(mockLogger.isErrorEnabled()).thenReturn(true);

        // When
        auditionLogger.logStandardProblemDetail(mockLogger, problemDetail, new RuntimeException("test"));

        // Then - verify the method was called with a formatted message
        verify(mockLogger).error(contains("Problem Detail Error:"), any(Exception.class));
    }

    @Test
    void testProblemDetailWithNullValues() {
        // Test problem detail logging with null values
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        // Leave title, detail, and instance as null

        when(mockLogger.isErrorEnabled()).thenReturn(true);

        // When
        auditionLogger.logStandardProblemDetail(mockLogger, problemDetail, new RuntimeException("test"));

        // Then
        verify(mockLogger).error(contains("Problem Detail Error:"), any(Exception.class));
    }

    @Test
    void testBasicErrorResponseMessageWithNullMessage() {
        // Test the private method createBasicErrorResponseMessage via public method
        when(mockLogger.isErrorEnabled()).thenReturn(true);

        // When
        auditionLogger.logHttpStatusCodeError(mockLogger, null, 404);

        // Then
        verify(mockLogger).error(contains("HTTP Error Response: Status Code: 404"));
    }

    @Test
    void testBasicErrorResponseMessageWithBlankMessage() {
        // Test with blank message
        when(mockLogger.isErrorEnabled()).thenReturn(true);

        // When
        auditionLogger.logHttpStatusCodeError(mockLogger, "   ", 500);

        // Then
        verify(mockLogger).error(contains("HTTP Error Response: Status Code: 500"));
    }

    @Test
    void testExceptionControllerAdviceConstants() {
        // Test that constants are accessible and have expected values
        assertEquals("API Error Occurred", ExceptionControllerAdvice.DEFAULT_TITLE);
    }

    @Test
    void testWebServiceConfigurationInstantiation() {
        // Test that configuration classes can be instantiated
        WebServiceConfiguration config = new WebServiceConfiguration();
        assertNotNull(config);

        // Test that ObjectMapper can be created
        assertNotNull(config.objectMapper());

        // Test that RestTemplate can be created
        assertNotNull(config.restTemplate());
    }

    @Test
    void testCacheConfigurationInstantiation() {
        // Test that cache configuration can be instantiated
        CacheConfiguration config = new CacheConfiguration();
        assertNotNull(config);

        // Test that CacheManager can be created
        assertNotNull(config.cacheManager());
    }

    @Test
    void testFilterLifecycleMethods() throws Exception {
        // Test filter lifecycle methods
        ResponseHeaderInjector filter = new ResponseHeaderInjector();

        // These methods should not throw exceptions
        filter.init(null);
        filter.destroy();
    }

    @Test
    void testLoggerMethodsWhenDisabled() {
        // Test all logger methods when logging is disabled
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        when(mockLogger.isDebugEnabled()).thenReturn(false);
        when(mockLogger.isWarnEnabled()).thenReturn(false);
        when(mockLogger.isErrorEnabled()).thenReturn(false);

        // When - call all logger methods
        auditionLogger.info(mockLogger, "test");
        auditionLogger.info(mockLogger, "test {}", "param");
        auditionLogger.info(mockLogger, "test {} {}", "param1", "param2");
        auditionLogger.debug(mockLogger, "test");
        auditionLogger.debug(mockLogger, "test {}", "param");
        auditionLogger.warn(mockLogger, "test");
        auditionLogger.warn(mockLogger, "test {}", "param");
        auditionLogger.error(mockLogger, "test");
        auditionLogger.error(mockLogger, "test {}", "param");
        auditionLogger.logErrorWithException(mockLogger, "test", new RuntimeException());
        auditionLogger.logStandardProblemDetail(mockLogger, ProblemDetail.forStatus(HttpStatus.BAD_REQUEST), new RuntimeException());
        auditionLogger.logHttpStatusCodeError(mockLogger, "test", 500);

        // Then - verify only the enabled checks were called
        verify(mockLogger, atLeast(3)).isInfoEnabled();
        verify(mockLogger, atLeast(2)).isDebugEnabled();
        verify(mockLogger, atLeast(2)).isWarnEnabled();
        verify(mockLogger, atLeast(4)).isErrorEnabled();

        // Verify no actual logging occurred
        verify(mockLogger, never()).info(anyString());
        verify(mockLogger, never()).debug(anyString());
        verify(mockLogger, never()).warn(anyString());
        verify(mockLogger, never()).error(anyString());
    }

    @Test
    void testConfigurationClassAnnotations() {
        // Verify configuration classes have proper annotations
        assertTrue(WebServiceConfiguration.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(CacheConfiguration.class.isAnnotationPresent(org.springframework.context.annotation.Configuration.class));
        assertTrue(CacheConfiguration.class.isAnnotationPresent(org.springframework.cache.annotation.EnableCaching.class));
        assertTrue(CacheConfiguration.class.isAnnotationPresent(org.springframework.scheduling.annotation.EnableScheduling.class));
        assertTrue(ResponseHeaderInjector.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testExceptionControllerAdviceAnnotations() {
        // Verify exception controller advice has proper annotations
        assertTrue(ExceptionControllerAdvice.class.isAnnotationPresent(org.springframework.web.bind.annotation.ControllerAdvice.class));
    }

    @Test
    void testAuditionLoggerAnnotations() {
        // Verify logger has proper annotations
        assertTrue(AuditionLogger.class.isAnnotationPresent(org.springframework.stereotype.Component.class));
    }

    @Test
    void testHttpMethodLogging() {
        // Test HTTP method and URI logging
        when(mockLogger.isInfoEnabled()).thenReturn(true);

        java.net.URI testUri = java.net.URI.create("http://example.com/test");
        org.springframework.http.HttpMethod method = org.springframework.http.HttpMethod.GET;
        org.springframework.http.HttpStatus status = org.springframework.http.HttpStatus.OK;

        // When
        auditionLogger.info(mockLogger, "Request: {} {}", method, testUri);
        auditionLogger.info(mockLogger, "Response: {} {} {}", status, method, testUri);

        // Then
        verify(mockLogger, times(2)).isInfoEnabled();
        verify(mockLogger).info("Request: {} {}", method, testUri);
        verify(mockLogger).info("Response: {} {} {}", status, method, testUri);
    }
}