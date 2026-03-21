package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {
    boolean existsByNombre(String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
    Optional<Rol> findByNombreIgnoreCase(String nombre);
}
