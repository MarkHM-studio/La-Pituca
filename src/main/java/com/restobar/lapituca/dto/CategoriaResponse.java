package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class CategoriaResponse {
    private Long id;
    private String nombre;
    private LocalDateTime fechaHora_Registro;
    private LocalDateTime fechaHora_Actualizacion;
}
