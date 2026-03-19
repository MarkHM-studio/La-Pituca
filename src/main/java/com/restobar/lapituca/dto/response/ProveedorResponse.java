package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProveedorResponse {
    private Long id;
    private String contacto;
    private String razonSocial;
    private String ruc;
    private String direccion;
    private String telefono;
    private String correo;
    private String estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;
}
