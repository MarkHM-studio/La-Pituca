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
    private String nombre;
    private String password;
    private String correo;
    private String estado;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;
}
