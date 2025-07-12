package com.audition.web.advice;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;

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

@ExtendWith(MockitoExtension.class)
class ExceptionControllerAdviceTest {

    @Mock
    private AuditionLogger auditionLogger;

    @InjectMocks
    private ExceptionControllerAdvice exceptionControllerAdvice;

    @BeforeEach
    void setUp() {
        // Setup is handled by Mockito annotations
    }

    @Test
    void testHandleHttpClientException() {
        // Given
        HttpClientErrorException exception = new HttpClientErrorException(HttpStatus.NOT_FOUND, "Not Found");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleHttpClientException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.NOT_FOUND.value(), result.getStatus());
        assertEquals("404 Not Found", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());

        verify(auditionLogger).logErrorWithException(any(Logger.class), anyString(), any(Exception.class));
    }

    @Test
    void testHandleSystemException() {
        // Given
        SystemException exception = new SystemException("Test error", "Test Title", 400);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(400, result.getStatus());
        assertEquals("Test error", result.getDetail());
        assertEquals("Test Title", result.getTitle());

        verify(auditionLogger).logErrorWithException(any(Logger.class), anyString(), any(Exception.class));
        verify(auditionLogger).logStandardProblemDetail(any(Logger.class), any(ProblemDetail.class), any(Exception.class));
    }

    @Test
    void testHandleSystemExceptionWithInvalidStatusCode() {
        // Given
        SystemException exception = new SystemException("Test error", "Test Title", 999);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Test error", result.getDetail());
        assertEquals("Test Title", result.getTitle());
    }

    @Test
    void testHandleSystemExceptionWithNullStatusCode() {
        // Given
        SystemException exception = new SystemException("Test error");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleSystemException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Test error", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithHttpRequestMethodNotSupportedException() {
        // Given
        HttpRequestMethodNotSupportedException exception = new HttpRequestMethodNotSupportedException("POST");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED.value(), result.getStatus());
        assertEquals("Request method 'POST' not supported", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithGenericException() {
        // Given
        RuntimeException exception = new RuntimeException("Generic error");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("Generic error", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithNullMessage() {
        // Given
        RuntimeException exception = new RuntimeException((String) null);

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("API Error occurred. Please contact support or administrator.", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }

    @Test
    void testHandleMainExceptionWithEmptyMessage() {
        // Given
        RuntimeException exception = new RuntimeException("");

        // When
        ProblemDetail result = exceptionControllerAdvice.handleMainException(exception);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), result.getStatus());
        assertEquals("API Error occurred. Please contact support or administrator.", result.getDetail());
        assertEquals("API Error Occurred", result.getTitle());
    }
}