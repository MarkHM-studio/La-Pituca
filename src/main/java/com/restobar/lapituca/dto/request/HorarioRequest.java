package com.restobar.lapituca.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioRequest {
    @NotNull(message = "La hora de inicio es obligatorio")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime hora_inicio;
    @NotNull(message = "La hora de fin es obligatorio")
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime hora_fin;
}
