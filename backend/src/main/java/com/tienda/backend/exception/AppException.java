package com.tienda.backend.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.Map;

@Getter
public class AppException extends RuntimeException {

    private final HttpStatus status;
    private final String errorCode;
    private final Map<String, Object> metadata;

    public AppException(HttpStatus status, String errorCode, Map<String, Object> metadata, Throwable cause) {
        super(errorCode, cause);
        this.status = status;
        this.errorCode = errorCode != null ? errorCode : "INTERNAL_ERROR";
        this.metadata = metadata != null ? metadata : Map.of();
    }

    public AppException(HttpStatus status, String errorCode, Map<String, Object> metadata) {
        this(status, errorCode, metadata, null);
    }

    public AppException(HttpStatus status, String errorCode) {
        this(status, errorCode, null, null);
    }

    public static AppException unauthorized(String errorCode) {
        return new AppException(HttpStatus.UNAUTHORIZED, errorCode);
    }

    public static AppException unauthorized(String errorCode, Map<String, Object> metadata) {
        return new AppException(HttpStatus.UNAUTHORIZED, errorCode, metadata);
    }

    public static AppException forbidden(String errorCode) {
        return new AppException(HttpStatus.FORBIDDEN, errorCode);
    }

    public static AppException notFound(String errorCode) {
        return new AppException(HttpStatus.NOT_FOUND, errorCode);
    }

    public static AppException notFound(String errorCode, Map<String, Object> metadata) {
        return new AppException(HttpStatus.NOT_FOUND, errorCode, metadata);
    }

    public static AppException conflict(String errorCode) {
        return new AppException(HttpStatus.CONFLICT, errorCode);
    }

    public static AppException badRequest(String errorCode, Map<String, Object> metadata) {
        return new AppException(HttpStatus.BAD_REQUEST, errorCode, metadata);
    }

    public static AppException internalError(String errorCode) {
        return new AppException(HttpStatus.INTERNAL_SERVER_ERROR, errorCode);
    }

    public static AppException internalError(String errorCode, Map<String, Object> metadata) {
        return new AppException(HttpStatus.INTERNAL_SERVER_ERROR, errorCode, metadata);
    }
}