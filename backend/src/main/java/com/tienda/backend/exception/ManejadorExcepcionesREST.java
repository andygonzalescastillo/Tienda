package com.tienda.backend.exception;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class ManejadorExcepcionesREST extends ResponseEntityExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ProblemDetail handleAppException(AppException ex) {
        log.warn("⚠️ AppException → errorCode={}, status={}, metadata={}, message={}",
                ex.getErrorCode(), ex.getStatus(), ex.getMetadata(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(ex.getStatus());
        problemDetail.setProperty("timestamp", Instant.now());
        if (ex.getErrorCode() != null) {
            problemDetail.setProperty("errorCode", ex.getErrorCode());
        }
        if (ex.getMetadata() != null && !ex.getMetadata().isEmpty()) {
            problemDetail.setProperty("metadata", ex.getMetadata());
        }
        return problemDetail;
    }

    @ExceptionHandler({AuthenticationException.class, BadCredentialsException.class, InternalAuthenticationServiceException.class})
    public ProblemDetail handleAuthenticationException(Exception ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setProperty("timestamp", Instant.now());
        
        String errorCode = switch (ex) {
            case BadCredentialsException _ -> "INVALID_CREDENTIALS";
            case InternalAuthenticationServiceException _ -> "INTERNAL_AUTH_ERROR";
            case Exception e -> {
                String message = e.getMessage();
                yield (message != null && !message.contains(" ")) ? message : "AUTH_ERROR";
            }
        };
        
        log.warn("🔐 AuthException → errorCode={}, type={}", errorCode, ex.getClass().getSimpleName());

        problemDetail.setProperty("errorCode", errorCode);
        return problemDetail;
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("🚫 AccessDenied → message={}", ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", "ACCESS_DENIED");
        return problemDetail;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            @NonNull HttpHeaders headers,
            @NonNull HttpStatusCode status,
            @NonNull WebRequest request) {

        ProblemDetail problemDetail = ex.getBody();
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", "VALIDATION_ERROR");

        Map<String, String> errores = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errores.put(error.getField(), error.getCode())
        );
        problemDetail.setProperty("errores", errores);

        log.warn("📋 ValidationError → campos={}", errores);

        return createResponseEntity(problemDetail, headers, status, request);
    }

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGlobalException(Exception ex) {
        log.error("💥 UnhandledException → type={}, message={}", ex.getClass().getSimpleName(), ex.getMessage(), ex);

        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("errorCode", "INTERNAL_ERROR");
        return problemDetail;
    }
}