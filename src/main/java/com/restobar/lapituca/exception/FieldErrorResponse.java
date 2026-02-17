package com.restobar.lapituca.exception;

import lombok.Data;

@Data
public class ApiValidationError extends ApiSubError {

    private String field;
    private Object rejectedValue;
    private String message;

    public ApiValidationError(String field, Object rejectedValue, String message) {
        this.field = field;
        this.rejectedValue = rejectedValue;
        this.message = message;
    }
}
