package com.sentinelai.repository;

import com.sentinelai.model.Recurso;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RecursoRepository extends JpaRepository<Recurso, Integer> {

    @EntityGraph(attributePaths = "tipoRecurso")
    List<Recurso> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = "tipoRecurso")
    List<Recurso> findByDisponibleOrderByIdAsc(boolean disponible);

    @EntityGraph(attributePaths = "tipoRecurso")
    Optional<Recurso> findConTipoById(Integer id);

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Integer id);

    /** Cuántas asignaciones (activas o históricas) tiene un recurso. */
    @Query(value = "SELECT COUNT(*) FROM asignaciones WHERE recurso_id = :recursoId", nativeQuery = true)
    long contarAsignaciones(@Param("recursoId") Integer recursoId);

    /** Deja disponibles los recursos que tenían una asignación activa en el incidente indicado. */
    @Modifying
    @Query(value = """
            UPDATE recursos SET disponible = TRUE
            WHERE id IN (SELECT recurso_id FROM asignaciones
                         WHERE incidente_id = :incidenteId AND fecha_liberacion IS NULL)
            """, nativeQuery = true)
    int liberarPorIncidente(@Param("incidenteId") Integer incidenteId);
}
