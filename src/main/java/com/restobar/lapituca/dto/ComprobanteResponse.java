package com.restobar.lapituca.dto;


import jakarta.annotation.security.DenyAll;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ComprobanteResponse {
    private Long id;
    private BigDecimal total;
    private BigDecimal IGV;
    private LocalDateTime fechaHora_venta;
    private String estado;
}
