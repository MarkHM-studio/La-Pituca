package com.restobar.lapituca.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioResponse {
    private Long id;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private LocalDateTime fechaHora_registro;
    private LocalDateTime fechaHora_actualizacion;
}
