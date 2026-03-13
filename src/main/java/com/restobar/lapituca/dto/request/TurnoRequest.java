package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 carácteres")
    private String nombre;

    @NotNull(message = "El horario es obligatorio")
    private Long horarioId;
}
