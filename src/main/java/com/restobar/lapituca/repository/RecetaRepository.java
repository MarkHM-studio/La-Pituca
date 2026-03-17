package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    List<Receta> findByProducto_IdOrderByIdAsc(Long productoId);
    List<Receta> findAllByOrderByProducto_IdAscIdAsc();
    boolean existsByProducto_Id(Long productoId);
    void deleteByProducto_Id(Long productoId);
    List<Receta> findByInsumo_Id(Long insumoId);
}