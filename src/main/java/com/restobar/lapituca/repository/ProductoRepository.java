package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    boolean existsByNombre (String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
