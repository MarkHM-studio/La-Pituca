package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Trabajador;
import com.restobar.lapituca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {
    boolean existsByDni(String DNI);
    boolean existsByDniAndIdNot(String DNI, Long id);
    Optional<Trabajador> findByUsuario(Usuario usuario);
}
