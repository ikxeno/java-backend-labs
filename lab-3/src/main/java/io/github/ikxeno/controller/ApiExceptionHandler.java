package io.github.ikxeno.controller;

import io.github.ikxeno.repository.RepositoryException;
import io.github.ikxeno.service.AirplaneNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger LOG = LoggerFactory
            .getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(AirplaneNotFoundException.class)
    ProblemDetail notFound(AirplaneNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND,
                exception.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    ProblemDetail invalidRequest(IllegalArgumentException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
                exception.getMessage());
    }

    @ExceptionHandler(RepositoryException.class)
    ProblemDetail databaseFailure(RepositoryException exception) {
        LOG.error("Database operation failed", exception);
        return ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR, "Database operation failed");
    }
}
