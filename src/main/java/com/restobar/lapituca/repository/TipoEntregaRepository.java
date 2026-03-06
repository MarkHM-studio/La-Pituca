package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.TipoEntrega;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoEntregaRepository extends JpaRepository<TipoEntrega, Long> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
