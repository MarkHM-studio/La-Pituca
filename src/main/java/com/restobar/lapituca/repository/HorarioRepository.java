package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalTime;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, Long> {
    boolean existsByHoraInicio(LocalTime horaInicio);
    boolean existsByHoraFin(LocalTime horaFin);
    boolean existsByHoraInicioAndIdNot(LocalTime horaInicio, Long id);
    boolean existsByHoraFinAndIdNot(LocalTime horaFin, Long id);

    boolean existsByHoraInicioAndHoraFin(LocalTime horaInicio, LocalTime horaFin);
    boolean existsByHoraInicioAndHoraFinAndIdNot(LocalTime horaInicio, LocalTime horaFin, Long id);
}
