package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponse {
    private Long id;
    private String username;
    private Integer tipo_usuario;
    private String estado;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;

    private Long rolId;
    private String rolNombre;
}
