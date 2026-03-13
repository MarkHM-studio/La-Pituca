package com.restobar.lapituca.exception;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j //Para usar Logs
public class GlobalExceptionHandler {

    private static final boolean DEV_MODE = true;

    //Validar datos enviados que recibe mi Dto Request, Detecta @Valid: @NotNull, @NotBlank, @Size, @Email, @Min, @Max
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidaciones(
            MethodArgumentNotValidException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación",
                request.getRequestURI()
        );

        ex.getBindingResult().getFieldErrors()
                .forEach(fieldError -> error.addSubError(
                        new FieldErrorResponse(
                                fieldError.getField(),
                                fieldError.getRejectedValue(),
                                fieldError.getDefaultMessage()
                        )
                ));

        return ResponseEntity.badRequest().body(error);
    }

    //Validación de parámetros @PathVariable, @RequestParam (hay 2)
    //api/horario/-1  | @Pathvariable negativo
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> manejarConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Error de validación",
                request.getRequestURI()
        );

        ex.getConstraintViolations().forEach(violation -> {
            error.addSubError(new FieldErrorResponse(
                    violation.getPropertyPath().toString(),
                    violation.getInvalidValue(),
                    violation.getMessage()
            ));
        });

        return ResponseEntity.badRequest().body(error);
    }

    //api/horario/abc    | @Pathvariable texto
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> manejarTypeMismatch(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request) {

        ErrorResponse error = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "El parámetro debe ser del tipo correcto",
                request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(error);
    }

    //Valida la restricciones de la base de datos (UNIQUE, FK, NOT NULL)   | Errores en la BD
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(
            DataIntegrityViolationException ex,
            HttpServletRequest request) {

        // Mensaje para el cliente
        String userMessage = "Violación de integridad de datos";

        if(DEV_MODE){
            log.error("Error de integridad: ", ex); // → Desarrollo
        } else{
            log.error("Error de integridad: {}", ex.getMessage()); // → Produccion
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                userMessage,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    //400 / 404 / 409           | Errores de negocio
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(
            ApiException ex,
            HttpServletRequest request) {

        ErrorCode code = ex.getErrorCode();

        ErrorResponse error = new ErrorResponse(
                code.getStatus().value(),
                code.getStatus().getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(code.getStatus())
                .body(error);
    }

    //Error general | Cualquier error no capturado anteriormente
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarErrorGeneral(
            Exception ex,
            HttpServletRequest request) {

        if(DEV_MODE){
            log.error("Error inesperado: ", ex); // → Desarrollo | ver stack trace completo para hacer debugging (proceso para identficar, analizar y eliminar error en el código fuente de un sw)
        } else{
            log.error("Error inesperado: {}", ex.getMessage()); // → Produccion
        }

        ErrorResponse error = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Ocurrió un error inesperado",
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}

