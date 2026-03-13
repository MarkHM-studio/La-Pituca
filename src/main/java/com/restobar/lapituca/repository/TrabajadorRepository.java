package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    boolean existsByDni(String DNI);
    boolean existsByDniAndIdNot(String DNI, Long id);
}
