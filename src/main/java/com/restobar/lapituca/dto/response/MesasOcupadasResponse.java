package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MesasOcupadasResponse {
    private Long mesaId;
    private String nombre;
    private Long grupoId;
    private String estadoMesa;
    private Long comprobanteId;
    private String estadoComprobante;
}