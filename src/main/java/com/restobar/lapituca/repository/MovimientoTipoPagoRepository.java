package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.MovimientoTipoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MovimientoTipoPagoRepository extends JpaRepository<MovimientoTipoPago, Long> {
    List<MovimientoTipoPago> findByComprobante_Id(Long comprobanteId);
}