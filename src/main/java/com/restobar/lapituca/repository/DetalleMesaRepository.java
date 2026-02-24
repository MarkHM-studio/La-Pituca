package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.DetalleMesa;
import com.restobar.lapituca.entity.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DetalleMesaRepository extends JpaRepository<DetalleMesa, Long> {
    List<DetalleMesa> findByGrupo_Id(Long id);
}
