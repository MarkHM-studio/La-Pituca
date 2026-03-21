package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistritoResponse {
    private Long id;
    private String nombre;
    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime fechaHoraActualizacion;
}