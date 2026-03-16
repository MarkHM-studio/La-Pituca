package com.restobar.lapituca.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReservaRequest {

    @NotNull(message = "La fecha de reserva es obligatoria")
    @FutureOrPresent(message = "La fecha debe ser hoy o futura")
    @JsonFormat(pattern = "dd/MM/yyyy")
    private LocalDate fechaReserva;

    @NotNull(message = "La hora de reserva es obligatoria")
    @JsonFormat(pattern = "HH:mm")
    private LocalTime horaReserva;

    @NotNull(message = "El número de personas es obligatorio")
    @Min(value = 1, message = "Debe haber al menos 1 persona")
    @Max(value = 20, message = "Máximo permitido 20 personas")
    private Integer numPersonas;

    @NotNull(message = "Debe indicar el usuario")
    private Long usuarioId;

    @NotEmpty(message = "Debe enviar al menos una mesa")
    private Set<@NotNull(message = "El id de mesa no puede ser nulo") Long> mesasId;

    @NotNull(message = "Debe indicar la sucursal")
    private Long sucursalId;

}