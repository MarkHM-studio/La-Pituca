package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    boolean existsByUsername(String nombre);
    boolean existsByUsernameAndIdNot(String nombre, Long id);
}
