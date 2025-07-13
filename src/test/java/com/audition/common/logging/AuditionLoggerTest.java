package com.audition.common.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.net.URI;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditionLoggerTest {

    private AuditionLogger auditionLogger;

    @Mock
    private Logger mockLogger;

    @BeforeEach
    void setUp() {
        auditionLogger = new AuditionLogger();
    }

    @Test
    void testInfoWithMessage() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        String message = "Test info message";

        // When
        auditionLogger.info(mockLogger, message);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).info(message);
    }

    @Test
    void testInfoWithMessageWhenDisabled() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        String message = "Test info message";

        // When
        auditionLogger.info(mockLogger, message);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger, never()).info(anyString());
    }

    @Test
    void testInfoWithMessageAndObject() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        String message = "Test info message: {}";
        Object object = "test object";

        // When
        auditionLogger.info(mockLogger, message, object);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).info(message, object);
    }

    @Test
    void testInfoWithMessageAndTwoObjects() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        String message = "Test info message: {} {}";
        Object object1 = "first";
        Object object2 = "second";

        // When
        auditionLogger.info(mockLogger, message, object1, object2);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).info(message, object1, object2);
    }

    @Test
    void testInfoWithHttpMethodAndUri() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        String message = "HTTP Request: {} {}";
        HttpMethod method = HttpMethod.GET;
        URI uri = URI.create("http://example.com/test");

        // When
        auditionLogger.info(mockLogger, message, method, uri);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).info(message, method, uri);
    }

    @Test
    void testInfoWithStatusCodeMethodAndUri() {
        // Given
        when(mockLogger.isInfoEnabled()).thenReturn(true);
        String message = "HTTP Response: {} {} {}";
        HttpStatus statusCode = HttpStatus.OK;
        HttpMethod method = HttpMethod.GET;
        URI uri = URI.create("http://example.com/test");

        // When
        auditionLogger.info(mockLogger, message, statusCode, method, uri);

        // Then
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).info(message, statusCode, method, uri);
    }

    @Test
    void testDebugWithMessage() {
        // Given
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        String message = "Test debug message";

        // When
        auditionLogger.debug(mockLogger, message);

        // Then
        verify(mockLogger).isDebugEnabled();
        verify(mockLogger).debug(message);
    }

    @Test
    void testDebugWithMessageAndObject() {
        // Given
        when(mockLogger.isDebugEnabled()).thenReturn(true);
        String message = "Test debug message: {}";
        Object object = "test object";

        // When
        auditionLogger.debug(mockLogger, message, object);

        // Then
        verify(mockLogger).isDebugEnabled();
        verify(mockLogger).debug(message, object);
    }

    @Test
    void testWarnWithMessage() {
        // Given
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        String message = "Test warn message";

        // When
        auditionLogger.warn(mockLogger, message);

        // Then
        verify(mockLogger).isWarnEnabled();
        verify(mockLogger).warn(message);
    }

    @Test
    void testWarnWithMessageAndObject() {
        // Given
        when(mockLogger.isWarnEnabled()).thenReturn(true);
        String message = "Test warn message: {}";
        Object object = "test object";

        // When
        auditionLogger.warn(mockLogger, message, object);

        // Then
        verify(mockLogger).isWarnEnabled();
        verify(mockLogger).warn(message, object);
    }

    @Test
    void testErrorWithMessage() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = "Test error message";

        // When
        auditionLogger.error(mockLogger, message);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(message);
    }

    @Test
    void testErrorWithMessageAndObject() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = "Test error message: {}";
        Object object = "test object";

        // When
        auditionLogger.error(mockLogger, message, object);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(message, object);
    }

    @Test
    void testLogErrorWithException() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = "Test error with exception";
        Exception exception = new RuntimeException("Test exception");

        // When
        auditionLogger.logErrorWithException(mockLogger, message, exception);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(message, exception);
    }

    @Test
    void testLogStandardProblemDetail() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Test Title");
        problemDetail.setDetail("Test Detail");
        problemDetail.setInstance(URI.create("/test"));
        Exception exception = new RuntimeException("Test exception");

        // When
        auditionLogger.logStandardProblemDetail(mockLogger, problemDetail, exception);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(contains("Problem Detail Error:"), eq(exception));
    }

    @Test
    void testLogHttpStatusCodeError() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = "Test HTTP error";
        Integer errorCode = 404;

        // When
        auditionLogger.logHttpStatusCodeError(mockLogger, message, errorCode);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(contains("HTTP Error Response: Status Code: 404, Message: Test HTTP error"));
    }

    @Test
    void testLogHttpStatusCodeErrorWithNullMessage() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = null;
        Integer errorCode = 500;

        // When
        auditionLogger.logHttpStatusCodeError(mockLogger, message, errorCode);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(contains("HTTP Error Response: Status Code: 500"));
    }

    @Test
    void testLogHttpStatusCodeErrorWithEmptyMessage() {
        // Given
        when(mockLogger.isErrorEnabled()).thenReturn(true);
        String message = "";
        Integer errorCode = 400;

        // When
        auditionLogger.logHttpStatusCodeError(mockLogger, message, errorCode);

        // Then
        verify(mockLogger).isErrorEnabled();
        verify(mockLogger).error(contains("HTTP Error Response: Status Code: 400"));
    }

    @Test
    void testWarnWithThreeParameters() {
        // Test the overloaded warn method that takes three string parameters
        String postId = "123";
        String message = "Test message";

        // When - calling the method that doesn't do anything (based on empty implementation)
        auditionLogger.warn(mockLogger, "Test", postId, message);

        // Then - verify no interactions with the mock logger since the method is empty
        verifyNoInteractions(mockLogger);
    }

    @Test
    void testAllLogLevelsWhenDisabled() {
        // Given - all log levels disabled
        when(mockLogger.isInfoEnabled()).thenReturn(false);
        when(mockLogger.isDebugEnabled()).thenReturn(false);
        when(mockLogger.isWarnEnabled()).thenReturn(false);
        when(mockLogger.isErrorEnabled()).thenReturn(false);

        // When
        auditionLogger.info(mockLogger, "info message");
        auditionLogger.debug(mockLogger, "debug message");
        auditionLogger.warn(mockLogger, "warn message");
        auditionLogger.error(mockLogger, "error message");

        // Then - verify only the enabled checks are called, no actual logging
        verify(mockLogger).isInfoEnabled();
        verify(mockLogger).isDebugEnabled();
        verify(mockLogger).isWarnEnabled();
        verify(mockLogger).isErrorEnabled();
        verifyNoMoreInteractions(mockLogger);
    }
}