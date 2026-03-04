package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarMesasRequest {
    private Long comprobanteId;
    private Set<Long> mesasId;
    private String nombreGrupo;
}
