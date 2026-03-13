package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {
    @NotBlank(message = "El username es obligatorio")
    @Size(min = 5, max = 50, message = "El username debe tener entre 5 y 50 caracteres")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 25, message = "La contraseña debe tener entre 8 y 25 caracteres")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,25}$",
            message = "La contraseña debe contener al menos una letra mayúscula, una minúscula, un número y un carácter especial"
    )
    private String password;
    /*(?=.*[a-z]) → al menos una minúscula
    (?=.*[A-Z]) → al menos una mayúscula
    (?=.*\\d) → al menos un dígito
    (?=.*[@$!%*?&]) → al menos un carácter especial (@ $ ! % * ? &)
    [A-Za-z\\d@$!%*?&]{8,25} → solo permite esos caracteres y respeta la longitud*/
    @NotNull(message = "El rol es obligatorio")
    private Long rolId;
}
