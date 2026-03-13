package com.restobar.lapituca.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioRequest {
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime hora_inicio;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime hora_fin;
}
