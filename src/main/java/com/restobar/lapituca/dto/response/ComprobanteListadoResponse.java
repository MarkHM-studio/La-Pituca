package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteListadoResponse {
    private Long id;
    private BigDecimal total;
    private BigDecimal IGV;
    private LocalDateTime fechaHora_apertura;
    private LocalDateTime fechaHora_venta;
    private String estado;
    private Long sucursalId;
    private Long usuarioCajaId;
}