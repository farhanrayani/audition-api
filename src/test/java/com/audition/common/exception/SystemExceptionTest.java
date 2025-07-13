package com.audition.common.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SystemExceptionTest {

    @Test
    void testDefaultConstructor() {
        SystemException exception = new SystemException();
        assertNotNull(exception);
        assertNull(exception.getMessage());
        assertNull(exception.getStatusCode());
        assertNull(exception.getTitle());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessage() {
        String message = "Test error message";
        SystemException exception = new SystemException(message);

        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndErrorCode() {
        String message = "Test error message";
        Integer errorCode = 400;

        SystemException exception = new SystemException(message, errorCode);

        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithMessageAndException() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Cause exception");

        SystemException exception = new SystemException(message, cause);

        assertEquals(message, exception.getMessage());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(cause, exception.getCause());
        assertNull(exception.getStatusCode());
        assertNull(exception.getDetail());
    }

    @Test
    void testConstructorWithDetailTitleAndErrorCode() {
        String detail = "Test detail";
        String title = "Test title";
        Integer errorCode = 404;

        SystemException exception = new SystemException(detail, title, errorCode);

        assertEquals(detail, exception.getMessage());
        assertEquals(detail, exception.getDetail());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
    }

    @Test
    void testConstructorWithDetailTitleAndException() {
        String detail = "Test detail";
        String title = "Test title";
        RuntimeException cause = new RuntimeException("Cause exception");

        SystemException exception = new SystemException(detail, title, cause);

        assertEquals(detail, exception.getMessage());
        assertEquals(detail, exception.getDetail());
        assertEquals(title, exception.getTitle());
        assertEquals(Integer.valueOf(500), exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithDetailErrorCodeAndException() {
        String detail = "Test detail";
        Integer errorCode = 503;
        RuntimeException cause = new RuntimeException("Cause exception");

        SystemException exception = new SystemException(detail, errorCode, cause);

        assertEquals(detail, exception.getMessage());
        assertEquals(detail, exception.getDetail());
        assertEquals(SystemException.DEFAULT_TITLE, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testConstructorWithAllParameters() {
        String detail = "Test detail";
        String title = "Test title";
        Integer errorCode = 422;
        RuntimeException cause = new RuntimeException("Cause exception");

        SystemException exception = new SystemException(detail, title, errorCode, cause);

        assertEquals(detail, exception.getMessage());
        assertEquals(detail, exception.getDetail());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }

    @Test
    void testDefaultTitleConstant() {
        assertEquals("API Error Occurred", SystemException.DEFAULT_TITLE);
    }

    @Test
    void testGetterMethods() {
        String detail = "Test detail";
        String title = "Test title";
        Integer errorCode = 400;
        RuntimeException cause = new RuntimeException("Cause exception");

        SystemException exception = new SystemException(detail, title, errorCode, cause);

        assertEquals(detail, exception.getDetail());
        assertEquals(title, exception.getTitle());
        assertEquals(errorCode, exception.getStatusCode());
        assertEquals(cause, exception.getCause());
    }
}