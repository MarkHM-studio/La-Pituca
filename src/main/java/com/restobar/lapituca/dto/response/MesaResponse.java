package com.restobar.lapituca.dto.response;

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
    private String estado;
    private LocalDateTime fechaHora_Registro;
    private LocalDateTime fechaHora_Actualizacion;
}
