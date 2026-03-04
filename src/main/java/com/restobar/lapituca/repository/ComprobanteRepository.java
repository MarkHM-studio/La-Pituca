package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Comprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ComprobanteRepository extends JpaRepository<Comprobante, Long> {
    Optional<Comprobante> findByGrupo_Id(Long grupoId);
}
