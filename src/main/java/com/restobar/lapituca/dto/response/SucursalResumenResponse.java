package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SucursalResumenResponse {
    private Long id;
    private String nombre;
    private String direccion;
    private String ruc;
}
