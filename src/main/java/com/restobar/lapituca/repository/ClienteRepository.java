package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Cliente;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    boolean existsByDni(@NotBlank(message = "El DNI es obligatorio") @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni);
    boolean existsByDniAndIdNot(@NotBlank(message = "El DNI es obligatorio") @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni, Long id);
    boolean existsByCorreo(@NotBlank(message = "El DNI es obligatorio") @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni);
    boolean existsByCorreoAndIdNot(@NotBlank(message = "El DNI es obligatorio") @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 dígitos") String dni, Long id);
}
