package com.restobar.lapituca.dto.auth;

import com.restobar.lapituca.dto.response.ClienteResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String token;
    private Long usuarioId;
    private String correo;
    private String rol;
    private String proveedor;
    private ClienteResponse cliente;
}