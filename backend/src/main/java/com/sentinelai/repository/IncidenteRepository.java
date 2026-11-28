package com.sentinelai.repository;

import com.sentinelai.model.Incidente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IncidenteRepository extends JpaRepository<Incidente, Integer> {

    @EntityGraph(attributePaths = {"categoria", "prioridad", "estado", "reportadoPor"})
    List<Incidente> findAllByOrderByFechaReporteDesc();

    @EntityGraph(attributePaths = {"categoria", "prioridad", "estado", "reportadoPor"})
    Optional<Incidente> findConDetalleById(Integer id);

    @EntityGraph(attributePaths = {"categoria", "prioridad", "estado", "reportadoPor"})
    Optional<Incidente> findByUuid(UUID uuid);
}
