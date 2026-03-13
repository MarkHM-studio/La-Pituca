package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.TipoJornada;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoJornadaRepository extends JpaRepository<TipoJornada, Long> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
