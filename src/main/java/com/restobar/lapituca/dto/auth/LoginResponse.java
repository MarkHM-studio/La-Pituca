package com.restobar.lapituca.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private Long usuarioId;
    private String correo;
    private String rol;
    private String proveedor;
}