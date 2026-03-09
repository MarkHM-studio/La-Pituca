package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long usuarioId;
    private String username;
    private String password;
    private String tipo_usuario;
    private String estado;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;
}
