package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequest {

    @NotBlank(message = "El contacto es obligatorio")
    @Size(min = 3, max = 50)
    private String contacto;

    @NotBlank(message = "La razón social es obligatoria")
    @Size(min = 5, max = 50)
    private String razonSocial;

    @NotBlank(message = "El RUC es obligatorio")
    @Size(min = 11, max = 11)
    private String ruc;

    @NotBlank(message = "La dirección es obligatoria")
    @Size(min = 5, max = 150)
    private String direccion;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 9, max = 9)
    private String telefono;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo debe ser válido")
    private String correo;

    @NotBlank(message = "El estado es obligatorio")
    @Size(min = 5, max = 25)
    private String estado;
}
