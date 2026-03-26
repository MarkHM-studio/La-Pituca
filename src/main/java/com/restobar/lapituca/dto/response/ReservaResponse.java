package com.restobar.lapituca.dto.response;

import lombok.*;

import java.time.*;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReservaResponse {

    private Long id;
    private LocalDate fechaReserva;
    private LocalTime horaReserva;
    private Integer numPersonas;
    private String estado;

    private Long usuarioId;
    private Long grupoId;
    private Long ultimaTransaccionId;
    private List<Long> transaccionesIds;

    private LocalDateTime fechaRegistro;
}