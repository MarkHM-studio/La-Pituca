package com.restobar.lapituca.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GrupoResponse {
    private Long id;
    private String nombre;
    private List<DetalleMesaResponse> detalleMesaResponse;
}
