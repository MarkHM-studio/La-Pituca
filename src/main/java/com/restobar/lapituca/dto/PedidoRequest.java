package com.restobar.lapituca.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.Set;

@Data
public class PedidoRequest {

    @Positive
    @Digits(integer = 3, fraction = 0)
    private Integer cantidad;

    private Long comprobanteId;
    private Long productoId;
    private Long tipoEntregaId;
    /*
    private Set<Long> mesasId;
    private String nombreGrupo;*/
}
