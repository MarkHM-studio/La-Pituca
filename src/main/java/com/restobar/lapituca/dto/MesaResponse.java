package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesaResponse {

    private Long id;
    private String nombre;
    private LocalDateTime fechaHora_Registro;
    private LocalDateTime fechaHora_Actualizacion;
}
