package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    Optional<Transaccion> findByMercadoPagoPaymentId(String mercadoPagoPaymentId);

    Optional<Transaccion> findTopByExternalReferenceOrderByFechaActualizacionDesc(String externalReference);

}