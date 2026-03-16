package com.restobar.lapituca.dto.response;

import lombok.*;
import java.time.*;

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
    private Long transaccionId;

    private LocalDateTime fechaRegistro;
}