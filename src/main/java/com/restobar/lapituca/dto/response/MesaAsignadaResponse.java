package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesaAsignadaResponse {
    private Long detalleMesaId;
    private Long mesaId;
    private String mesaNombre;
    private String estadoMesa;
}