package com.restobar.lapituca.exception;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    private List<FieldErrorResponse> subErrors;

    public ErrorResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int status, String error, String message, String path) {
        this();
        this.status = status;
        this.error = error;
        this.message = message;
        this.path = path;
    }

    public void addSubError(FieldErrorResponse subError) {
        this.subErrors = this.subErrors == null ? new java.util.ArrayList<>() : this.subErrors;
        this.subErrors.add(subError);
    }
}
