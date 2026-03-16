package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.Mesa;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface MesaRepository extends JpaRepository<Mesa, Long> {
    boolean existsByNombre (String nombre);
    boolean existsByNombreAndIdNot(String nombre, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT m
           FROM Mesa m
           WHERE m.id IN :mesasId
           """)
    List<Mesa> findMesasForUpdate(@Param("mesasId") Set<Long> mesasId);
}
