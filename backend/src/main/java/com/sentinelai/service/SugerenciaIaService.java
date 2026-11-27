package com.sentinelai.service;

import com.sentinelai.dto.SugerenciaIaRequest;
import com.sentinelai.dto.ValidacionIaRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.Map;
import java.util.List;

@Service
public class SugerenciaIaService {

    private final JdbcTemplate jdbc;

    public SugerenciaIaService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public Map<String, Object> guardar(SugerenciaIaRequest r) {

        List<Long> incidentes = jdbc.queryForList(
            "SELECT id FROM incidentes WHERE id = ? FOR UPDATE",
            Long.class, r.incidenteId()
        );

        if (incidentes.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Incidente no encontrado"
            );
        }

        Long id = jdbc.queryForObject("""
            INSERT INTO sugerencias_ia
            (incidente_id, categoria_sugerida_id,
             prioridad_sugerida_id, justificacion, fuente)
            VALUES (?, ?, ?, ?, ?)
            RETURNING id
            """,
            Long.class,
            r.incidenteId(),
            r.categoriaId(),
            r.prioridadId(),
            r.justificacion(),
            r.fuente()
        );

        return consultar(id);
    }

    public Map<String, Object> consultar(Long id) {

        List<Map<String, Object>> filas = jdbc.queryForList(
            "SELECT * FROM sugerencias_ia WHERE id = ?", id
        );

        if (filas.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Sugerencia no encontrada"
            );
        }

        return filas.get(0);
    }

    @Transactional
    public Map<String, Object> validar(
        Long id, ValidacionIaRequest r
    ) {

        List<Map<String, Object>> filas = jdbc.queryForList(
            "SELECT * FROM sugerencias_ia WHERE id = ? FOR UPDATE",
            id
        );

        if (filas.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Sugerencia no encontrada"
            );
        }

        Map<String, Object> original = filas.get(0);

        if (original.get("aceptada") != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT, "Sugerencia ya validada"
            );
        }

        long categoria = ((Number) original.get(
            "categoria_sugerida_id")).longValue();

        long prioridad = ((Number) original.get(
            "prioridad_sugerida_id")).longValue();

        boolean coincide =
            categoria == r.categoriaId() &&
            prioridad == r.prioridadId();

        if (r.aceptada() != coincide) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "La decision no coincide con los valores enviados"
            );
        }

        jdbc.update("""
            UPDATE incidentes
            SET categoria_id = ?, prioridad_id = ?
            WHERE id = ?
            """,
            r.categoriaId(),
            r.prioridadId(),
            original.get("incidente_id")
        );

        jdbc.update("""
            UPDATE sugerencias_ia
            SET aceptada = ?
            WHERE id = ?
            """,
            r.aceptada(), id
        );

        return consultar(id);
    }
}