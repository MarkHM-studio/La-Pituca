package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalRequest {
    @NotBlank(message = "La nombre es obligatorio")
    @Size(min = 5, max = 50, message = "El nombre debe tener entre 5 y 50 carácteres")
    private String nombre;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 150, message = "La dirección debe tener entre 5 y 150 carácteres")
    private String direccion;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11, message = "El RUC debe tener exactamente 11 digitos")
    private String RUC;
}
