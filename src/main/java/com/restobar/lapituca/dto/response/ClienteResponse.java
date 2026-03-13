package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String correo;
    private String estado;
    private String tipoCliente;
    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime fechaHoraActualizacion;

    // Relación
    private Long usuarioId;
    private String username;
}
