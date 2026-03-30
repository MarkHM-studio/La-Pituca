package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Cliente;
import com.restobar.lapituca.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdNot(String correo, Long id);
    boolean existsByTelefono(String telefono);
    boolean existsByTelefonoAndIdNot(String telefono, Long id);
    Optional<Cliente> findByCorreo(String correo);
    Optional<Cliente> findByUsuario(Usuario usuario);
}