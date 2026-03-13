package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolRequest {
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 4, max = 50, message = "El nombre de tener entre 4 y 50 caracteres")
    private String nombre;
}
