package com.restobar.lapituca.repository;


import com.restobar.lapituca.entity.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    boolean existsByCorreo(String correo);
    boolean existsByCorreoAndIdNot(String correo, Long id);
    boolean existsByRUC(String ruc);
    boolean existsByRUCAndIdNot(String ruc, Long id);
}
