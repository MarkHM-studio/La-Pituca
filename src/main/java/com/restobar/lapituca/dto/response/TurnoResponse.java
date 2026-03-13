package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponse {
    private Long id;
    private String nombre;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;
    private HorarioResponse horarioResponse;
}
