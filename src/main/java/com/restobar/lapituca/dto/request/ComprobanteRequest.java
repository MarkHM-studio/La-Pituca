package com.restobar.lapituca.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteRequest {
    @NotNull(message = "La sucursal es obligatoria")
    private Long sucursalId;
}
