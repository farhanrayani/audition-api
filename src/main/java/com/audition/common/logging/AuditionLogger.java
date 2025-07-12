package com.audition.common.logging;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
public class AuditionLogger {

    public void info(final Logger logger, final String message) {
        if (logger.isInfoEnabled()) {
            logger.info(message);
        }
    }

    public void info(final Logger logger, final String message, final Object object) {
        if (logger.isInfoEnabled()) {
            logger.info(message, object);
        }
    }

    public void debug(final Logger logger, String s, final String message) {
        if (logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

    public void warn(final Logger logger, final String message) {
        if (logger.isWarnEnabled()) {
            logger.warn(message);
        }
    }

    public void error(final Logger logger, final String message) {
        if (logger.isErrorEnabled()) {
            logger.error(message);
        }
    }

    public void logErrorWithException(final Logger logger, final String message, final Exception e) {
        if (logger.isErrorEnabled()) {
            logger.error(message, e);
        }
    }

    public void logStandardProblemDetail(final Logger logger, final ProblemDetail problemDetail, final Exception e) {
        if (logger.isErrorEnabled()) {
            final var message = createStandardProblemDetailMessage(problemDetail);
            logger.error(message, e);
        }
    }

    public void logHttpStatusCodeError(final Logger logger, final String message, final Integer errorCode) {
        if (logger.isErrorEnabled()) {
            logger.error(createBasicErrorResponseMessage(errorCode, message) + "\n");
        }
    }

    private String createStandardProblemDetailMessage(final ProblemDetail standardProblemDetail) {
        final StringBuilder sb = new StringBuilder();
        sb.append("Problem Detail Error: ");

        if (standardProblemDetail.getTitle() != null) {
            sb.append("Title: ").append(standardProblemDetail.getTitle()).append(", ");
        }

        if (standardProblemDetail.getDetail() != null) {
            sb.append("Detail: ").append(standardProblemDetail.getDetail()).append(", ");
        }

        if (standardProblemDetail.getStatus() != 0) {
            sb.append("Status: ").append(standardProblemDetail.getStatus()).append(", ");
        }

        if (standardProblemDetail.getInstance() != null) {
            sb.append("Instance: ").append(standardProblemDetail.getInstance());
        }

        return sb.toString();
    }

    private String createBasicErrorResponseMessage(final Integer errorCode, final String message) {
        final StringBuilder sb = new StringBuilder();
        sb.append("HTTP Error Response: ");
        sb.append("Status Code: ").append(errorCode);

        if (StringUtils.isNotBlank(message)) {
            sb.append(", Message: ").append(message);
        }

        return sb.toString();
    }

    public void info(Logger log, String s, HttpMethod method, URI uri) {
    }

    public void info(Logger log, String s, HttpStatusCode statusCode, HttpMethod method, URI uri) {
    }
}