package com.restobar.lapituca.exception;

import org.springframework.http.HttpStatus;

//Centralizamos todos los posibles status de errores
public enum ErrorCode {

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "Recurso no encontrado"),
    VALIDATION_ERROR(HttpStatus.BAD_REQUEST, "Error de validación"),
    BUSINESS_RULE_ERROR(HttpStatus.CONFLICT, "Regla de negocio violada"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "No autenticado"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "No autorizado");

    private final HttpStatus httpStatus;
    private final String defaultMessage;

    ErrorCode(HttpStatus status, String defaultMessage) {
        this.httpStatus = status;
        this.defaultMessage = defaultMessage;
    }

    public HttpStatus getStatus() {
        return httpStatus;
    }

    public String getDefaultMessage() {
        return defaultMessage;
    }
}