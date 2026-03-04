package com.restobar.lapituca.repository;

import com.restobar.lapituca.entity.TipoBilleteraVirtual;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoBilleteraVirtualRepository extends JpaRepository<TipoBilleteraVirtual, Long> {
}
