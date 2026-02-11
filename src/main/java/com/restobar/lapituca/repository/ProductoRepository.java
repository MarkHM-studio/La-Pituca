package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
