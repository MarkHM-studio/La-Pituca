package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.MovimientoInsumo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoInsumoRepository extends JpaRepository<MovimientoInsumo, Long> {
    @Query("SELECT mi FROM MovimientoInsumo mi ORDER BY mi.fechaHora_registro DESC")
    List<MovimientoInsumo> listarTodosOrdenadosPorRegistroDesc();

    @Query("""
        SELECT mi
        FROM MovimientoInsumo mi
        WHERE mi.comprobante.id = :comprobanteId
        ORDER BY mi.fechaHora_registro ASC
    """)
    List<MovimientoInsumo> listarPorComprobanteOrdenadoPorRegistroAsc(@Param("comprobanteId") Long comprobanteId);
}