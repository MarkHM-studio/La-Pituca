package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistritoRequest {

    @NotBlank(message = "El nombre del distrito es obligatorio")
    @Size(min = 3, max = 80, message = "El distrito debe tener entre 3 y 80 caracteres")
    private String nombre;
}