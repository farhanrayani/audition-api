package com.audition.common.exception;

import lombok.Getter;

/**
 * Custom exception for application-specific errors.
 *
 * This exception extends RuntimeException and provides structured
 * error information including:
 * - HTTP status codes
 * - Detailed error messages
 * - Custom error titles
 * - Cause chaining
 *
 * Used throughout the application for consistent error handling and
 * logging purposes.
 *
 * @author Farhan Rayani
 */
@Getter
public class SystemException extends RuntimeException {

    private static final long serialVersionUID = -5876728854007114881L;

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private Integer statusCode;
    private String title;
    private String detail;

    public SystemException() {
        super();
    }

    public SystemException(final String message) {
        super(message);
        this.title = DEFAULT_TITLE;
    }

    public SystemException(final String message, final Integer errorCode) {
        super(message);
        this.title = DEFAULT_TITLE;
        this.statusCode = errorCode;
    }

    public SystemException(final String message, final Throwable exception) {
        super(message, exception);
        this.title = DEFAULT_TITLE;
    }

    public SystemException(final String detail, final String title, final Integer errorCode) {
        super(detail);
        this.statusCode = errorCode;
        this.title = title;
        this.detail = detail;
    }

    public SystemException(final String detail, final String title, final Throwable exception) {
        super(detail, exception);
        this.title = title;
        this.statusCode = 500;
        this.detail = detail;
    }

    public SystemException(final String detail, final Integer errorCode, final Throwable exception) {
        super(detail, exception);
        this.statusCode = errorCode;
        this.title = DEFAULT_TITLE;
        this.detail = detail;
    }

    public SystemException(final String detail, final String title, final Integer errorCode, final Throwable exception) {
        super(detail, exception);
        this.statusCode = errorCode;
        this.title = title;
        this.detail = detail;
    }
}
