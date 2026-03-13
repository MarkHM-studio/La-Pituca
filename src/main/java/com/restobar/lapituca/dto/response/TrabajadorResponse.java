package com.restobar.lapituca.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorResponse {
    private Long id;
    private String nombre;
    private String apellido;
    private String dni;
    private String telefono;
    private String correo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
    private LocalDateTime fechaHoraRegistro;
    private LocalDateTime fechaHoraActualizacion;

    // Relaciones
    private Long usuarioId;
    private String username;

    private Long tipoJornadaId;
    private String nombreJornada;

    private Long turnoId;
    private String nombreTurno;
}
