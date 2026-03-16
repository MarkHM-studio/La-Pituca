package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MesasDisponiblesResponse {

    private Long mesaId;

    private String nombre;

    private boolean ocupada;

}
