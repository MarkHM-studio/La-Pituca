package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Mesa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    boolean existsByNombre (String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
