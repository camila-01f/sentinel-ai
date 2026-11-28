package com.sentinelai.repository;

import com.sentinelai.model.Estado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstadoRepository extends JpaRepository<Estado, Integer> {
    Optional<Estado> findByNombreIgnoreCase(String nombre);
    List<Estado> findAllByOrderByOrdenAsc();
}
