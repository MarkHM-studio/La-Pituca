package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre debe tener entre 4 y 50 caracteres")
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(min = 5, max = 50, message = "El apellido debe tener entre 5 y 50 caracteres")
    private String apellido;

    @NotBlank(message = "El DNI es obligatorio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos")
    private String dni;
     /* regexp = expresión regular (Regular Expression) que se usa para validar el formato de un texto.
    evita: AAAAAAAAA, 1234abcd
    \d = un número del 0-9
    {8} = exactamente 8 números

    Otra forma:
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener 8 dígitos")
    */
    @NotBlank(message = "El teléfono es obligatorio")
    @Pattern(regexp = "\\d{9}", message = "El teléfono debe tener 9 dígitos")
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotBlank(message = "El estado es obligatorio")
    @Size(min = 5, max = 25, message = "El estado debe tener entre 5 y 25 caracteres")
    private String estado;

    // Relaciones
    @NotNull(message = "El usuario es obligatorio")
    private Long usuarioId;

    @NotNull(message = "El tipo de jornada es obligatorio")
    private Long tipoJornadaId;

    @NotNull(message = "El turno es obligatorio")
    private Long turnoId;
}