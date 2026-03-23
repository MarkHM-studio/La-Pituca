package com.restobar.lapituca.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthMeResponse {
    private Long usuarioId;
    private String correo;
    private String rol;
    private String proveedor;
    private String estado;
    private Long clienteId;
    private String nombreCompleto;
    private String foto;
}