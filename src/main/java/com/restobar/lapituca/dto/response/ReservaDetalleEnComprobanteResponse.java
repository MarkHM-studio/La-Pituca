package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaDetalleEnComprobanteResponse {
    private Long id;
    private LocalDate fechaReserva;
    private LocalTime horaReserva;
    private Integer numPersonas;
    private String estado;
    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaVerificacionReserva;
    private ClienteResumenResponse cliente;
}
