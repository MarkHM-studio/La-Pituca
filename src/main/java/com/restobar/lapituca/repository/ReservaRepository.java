package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Reserva;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long>{

    Optional<Reserva> findByGrupo_Id(Long grupoId);

    @Query("""
           SELECT r
           FROM Reserva r
           WHERE r.fecha_reserva = :fecha
           AND r.hora_reserva >= :inicio
           AND r.hora_reserva < :fin
           AND r.estado <> 'CANCELADO'
           AND r.estado <> 'EXPIRADO'
           AND NOT (
               r.estado = 'ESPERANDO PAGO'
               AND r.fechaHora_expiracionPago IS NOT NULL
               AND r.fechaHora_expiracionPago < CURRENT_TIMESTAMP
           )
           """)
    List<Reserva> findReservasEnRango(
            @Param("fecha") LocalDate fecha,
            @Param("inicio") LocalTime inicio,
            @Param("fin") LocalTime fin
    );


    /*Detecta reservas que chocan (se solapan) con otra reserva en las mismas mesas.
    //19:00 - 20:00 reserva uno
    //20:00 - 21:00 reserva dos

    //19:00 - 20:00 reserva uno
        //19:30 - 20:30 reserva dos
    //inicio 2 < fin 1*/
    @Query("""
        SELECT DISTINCT r
        FROM Reserva r
        JOIN r.grupo g
        JOIN g.detalleMesas dm
        WHERE r.fecha_reserva = :fecha
        AND r.estado <> 'CANCELADO'
        AND r.estado <> 'EXPIRADO'
         AND NOT (
            r.estado = 'ESPERANDO PAGO'
            AND r.fechaHora_expiracionPago IS NOT NULL
            AND r.fechaHora_expiracionPago < CURRENT_TIMESTAMP
        )
        AND dm.mesa.id IN :mesasId
        AND r.hora_reserva < :fin
        AND r.hora_reserva > :inicioMenosUnaHora
    """)
    List<Reserva> findReservasSolapadas(
            @Param("fecha") LocalDate fecha,
            @Param("inicioMenosUnaHora") LocalTime inicioMenosUnaHora,
            @Param("fin") LocalTime fin,
            @Param("mesasId") Set<Long> mesasId
    );

    @Modifying
    @Query("""
        UPDATE Reserva r
        SET r.estado = 'EXPIRADO'
        WHERE r.estado = 'ESPERANDO PAGO'
        AND r.fechaHora_expiracionPago IS NOT NULL
        AND r.fechaHora_expiracionPago < CURRENT_TIMESTAMP
    """)
    int marcarReservasExpiradas();

    @Modifying
    @Query("""
        UPDATE Reserva r
        SET r.estado = 'NO_SHOW'
        WHERE r.estado = 'PAGADO'
        AND r.fechaHora_verificacionReserva IS NULL
        AND (r.fecha_reserva < CURRENT_DATE
            OR (r.fecha_reserva = CURRENT_DATE AND r.hora_reserva < :horaLimite))
    """)
    int marcarReservasNoShow(@Param("horaLimite") LocalTime horaLimite);
}