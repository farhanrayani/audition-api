package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.client.HttpClientErrorException;

import java.net.URI;

@ExtendWith(MockitoExtension.class)
class EnhancedExceptionControllerAdviceTest {

    @Mock
    private AuditionLogger auditionLogger;

    @InjectMocks
    private ExceptionControllerAdvice exceptionControllerAdvice;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito annotations
    }

    @Test
    void testHandleHttpClientExceptionBadRequest() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.BAD_REQUEST, "Bad Request");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleHttpClientException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getStatus());
        assertEquals("400 Bad Request", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());

        verify(auditionLogger).logErrorWithException(any(Logger.class), anyString(), any(Exception.class));
    }

    @Test
    void testHandleHttpClientExceptionUnauthorized() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.UNAUTHORIZED, "Unauthorized");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleHttpClientException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getStatus());
        assertEquals("401 Unauthorized", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleHttpClientExceptionForbidden() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.FORBIDDEN, "Forbidden");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleHttpClientException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.FORBIDDEN.value(), result.getStatus());
        assertEquals("403 Forbidden", result.getDetail());
    }

    @Test
    void testHandleSystemExceptionWithValidStatusCode() {
        // Given
        SystemException exception = new SystemException("Resource not found", "Not Found", 404);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(404, result.getStatus());
        assertEquals("Resource not found", result.getDetail());
        assertEquals("Not Found", result.getTitle());

        verify(auditionLogger).logErrorWithException(any(Logger.class), anyString(), any(Exception.class));
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), any(ProblemDetail.class), any(Exception.class));
    }

    @Test
    void testHandleSystemExceptionWithInternalServerError() {
        // Given
        SystemException exception = new SystemException("Internal error", "Server Error", 500);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(500, result.getStatus());
        assertEquals("Internal error", result.getDetail());
        assertEquals("Server Error", result.getTitle());
    }

    @Test
    void testHandleSystemExceptionWithCustomStatusCode() {
        // Given
        SystemException exception = new SystemException("Conflict occurred", "Conflict", 409);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(409, result.getStatus());
        assertEquals("Conflict occurred", result.getDetail());
        assertEquals("Conflict", result.getTitle());
    }

    @Test
    void testHandleSystemExceptionWithDefaultTitle() {
        // Given
        SystemException exception = new SystemException("Some error occurred");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Some error occurred", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithIllegalArgumentException() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Invalid argument", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithNullPointerException() {
        // Given
        NullPointerException exception = new NullPointerException("Null pointer occurred");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Null pointer occurred", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithHttpClientErrorException() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Resource not found");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("404 Resource not found", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testCreateProblemDetailWithSystemException() {
        // Given
        SystemException exception = new SystemException("Test detail", "Test Title", 422);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(422, result.getStatus());
        assertEquals("Test detail", result.getDetail());
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void testGetHttpStatusCodeFromExceptionEdgeCases() {
        // Test with HttpRequestMethodNotSupportedException
        HttpRequestMethodNotSupportedException methodNotSupported =
                new HttpRequestMethodNotSupportedException("DELETE");

        ProblemDetail result1 = exceptionControllerAdvice.handleMainException(methodNotSupported);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result1.getStatus());

        // Test with generic RuntimeException
        RuntimeException runtimeException = new RuntimeException("Runtime error");
        ProblemDetail result2 = exceptionControllerAdvice.handleMainException(runtimeException);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result2.getStatus());
    }

    @Test
    void testDefaultTitleConstant() {
        // Verify the constant is accessible and has the expected value
        assertEquals("API Error Occurred", ExceptionControllerAdvice.DEFAULT_TITLE);
    }

    @Test
    void testMessageExtractionFromExceptions() {
        // Test with blank message
        SystemException blankMessage = new SystemException("   ");
        ProblemDetail result1 = exceptionControllerAdvice.handleSystemException(blankMessage);
        assertEquals("API Error occurred. Please contact support or administrator.", result1.getDetail());

        // Test with normal message
        SystemException normalMessage = new SystemException("Normal error message");
        ProblemDetail result2 = exceptionControllerAdvice.handleSystemException(normalMessage);
        assertEquals("Normal error message", result2.getDetail());
    }

    @Test
    void testLoggingVerification() {
        // Given
        SystemException exception = new SystemException("Test logging", "Test Title", 400);

        // When
        exceptionControllerAdvice.handleSystemException(exception);

        // Then
        verify(auditionLogger).logErrorWithException(any(Logger.class),
                contains("System exception occurred"), any(Exception.class));
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class),
                any(ProblemDetail.class), any(Exception.class));
    }

    @Test
    void testProblemDetailProperties() {
        // Given
        SystemException exception = new SystemException("Detailed error message", "Custom Title", 418);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(418, result.getStatus());
        assertEquals("Detailed error message", result.getDetail());
        assertEquals("Custom Title", result.getTitle());
        assertNull(result.getInstance()); // Should be null by default
        assertNull(result.getType()); // Should be null by default
    }
}