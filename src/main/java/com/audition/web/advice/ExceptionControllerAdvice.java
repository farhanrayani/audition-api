package com.audition.web.advice;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.METHOD_NOT_ALLOWED;

import com.audition.common.exception.SystemException;
import com.audition.common.logging.AuditionLogger;
import io.micrometer.common.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

/**
 * Centralized exception handler for the audition application.
 *
 * This advice handles all exceptions thrown by controllers and converts
 * them to standardized ProblemDetail responses following RFC 7807.
 *
 * Handles the following exception types:
 * - SystemException - Application-specific errors
 * - HttpClientErrorException - HTTP client errors
 * - Exception - Generic exceptions
 *
 * @author Farhan Rayani
 * @since 1.0.0
 * @see SystemException
 */

@ControllerAdvice
public class ExceptionControllerAdvice extends ResponseEntityExceptionHandler {

    public static final String DEFAULT_TITLE = "API Error Occurred";
    private static final Logger LOG = LoggerFactory.getLogger(ExceptionControllerAdvice.class);
    private static final String ERROR_MESSAGE = " Error Code from Exception could not be mapped to a valid HttpStatus Code - ";
    private static final String DEFAULT_MESSAGE = "API Error occurred. Please contact support or administrator.";

    @Autowired
    private AuditionLogger logger;

    @ExceptionHandler(HttpClientErrorException.class)
    ProblemDetail handleHttpClientException(final HttpClientErrorException e) {
        logger.logErrorWithException(LOG, "HTTP Client Error occurred", e);
        return createProblemDetail(e, e.getStatusCode());
    }

    @ExceptionHandler(Exception.class)
    ProblemDetail handleMainException(final Exception e) {
        logger.logErrorWithException(LOG, "General exception occurred", e);
        final HttpStatusCode status = getHttpStatusCodeFromException(e);
        ProblemDetail problemDetail = createProblemDetail(e, status);
        logger.logStandardProblemDetail(LOG, problemDetail, e);
        return problemDetail;
    }

    @ExceptionHandler(SystemException.class)
    ProblemDetail handleSystemException(final SystemException e) {
        logger.logErrorWithException(LOG, "System exception occurred", e);
        final HttpStatusCode status = getHttpStatusCodeFromSystemException(e);
        ProblemDetail problemDetail = createProblemDetail(e, status);
        logger.logStandardProblemDetail(LOG, problemDetail, e);
        return problemDetail;
    }

    private ProblemDetail createProblemDetail(final Exception exception,
                                              final HttpStatusCode statusCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatus(statusCode);
        problemDetail.setDetail(getMessageFromException(exception));
        if (exception instanceof SystemException) {
            problemDetail.setTitle(((SystemException) exception).getTitle());
        } else {
            problemDetail.setTitle(DEFAULT_TITLE);
        }
        return problemDetail;
    }

    private String getMessageFromException(final Exception exception) {
        if (StringUtils.isNotBlank(exception.getMessage())) {
            return exception.getMessage();
        }
        return DEFAULT_MESSAGE;
    }

    private HttpStatusCode getHttpStatusCodeFromSystemException(final SystemException exception) {
        try {
            if (exception.getStatusCode() != null) {
                return HttpStatusCode.valueOf(exception.getStatusCode());
            }
            return INTERNAL_SERVER_ERROR;
        } catch (final IllegalArgumentException iae) {
            logger.info(LOG, ERROR_MESSAGE + exception.getStatusCode());
            return INTERNAL_SERVER_ERROR;
        }
    }

    private HttpStatusCode getHttpStatusCodeFromException(final Exception exception) {
        if (exception instanceof HttpClientErrorException) {
            return ((HttpClientErrorException) exception).getStatusCode();
        } else if (exception instanceof HttpRequestMethodNotSupportedException) {
            return METHOD_NOT_ALLOWED;
        }
        return INTERNAL_SERVER_ERROR;
    }
}