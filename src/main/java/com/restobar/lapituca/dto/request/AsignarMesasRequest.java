package com.restobar.lapituca.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

import jakarta.validation.constraints.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignarMesasRequest {

    @NotNull(message = "El comprobante es obligatorio")
    private Long comprobanteId;

    @NotEmpty(message = "Debe enviar al menos una mesa")
    private Set<@NotNull(message = "El id de mesa no puede ser nulo") Long> mesasId; //Set<@NotNull Long> valida cada elemento del set.

    @Size(max = 50, message = "El nombre del grupo no puede superar 50 caracteres")
    private String nombreGrupo;
}