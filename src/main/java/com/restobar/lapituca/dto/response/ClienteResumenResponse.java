package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResumenResponse {
    private Long id;
    private String nombreCompleto;
    private String correo;
    private String telefono;
}