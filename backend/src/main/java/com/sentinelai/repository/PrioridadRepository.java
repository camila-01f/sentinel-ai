package com.sentinelai.repository;

import com.sentinelai.model.Prioridad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrioridadRepository extends JpaRepository<Prioridad, Integer> {
    List<Prioridad> findAllByOrderByNivelAsc();
}
