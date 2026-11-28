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
        Long id, ValidacionIaRequest r, Long usuarioId
    ) {

        // 1. Comprobar que el usuario sea un supervisor activo.
        List<Map<String, Object>> usuarios = jdbc.queryForList("""
            SELECT u.id, rol.nombre AS rol
            FROM usuarios u
            JOIN roles rol ON rol.id = u.rol_id
            WHERE u.id = ? AND u.activo = TRUE
            FOR UPDATE OF u
            """, usuarioId);

        if (usuarios.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Supervisor no encontrado o inactivo"
            );
        }

        if (!"SUPERVISOR".equals(usuarios.get(0).get("rol"))) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Solo un supervisor puede validar sugerencias"
            );
        }

        // 2. Bloquear la sugerencia y su incidente.
        List<Map<String, Object>> filas = jdbc.queryForList("""
            SELECT s.*,
                   i.estado_id AS estado_incidente
            FROM sugerencias_ia s
            JOIN incidentes i ON i.id = s.incidente_id
            WHERE s.id = ?
            FOR UPDATE OF s, i
            """, id);

        if (filas.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Sugerencia no encontrada"
            );
        }

        Map<String, Object> original = filas.get(0);

        if (original.get("aceptada") != null) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Sugerencia ya validada"
            );
        }

        long categoria = ((Number) original.get(
            "categoria_sugerida_id")).longValue();

        long prioridad = ((Number) original.get(
            "prioridad_sugerida_id")).longValue();

        long incidenteId = ((Number) original.get(
            "incidente_id")).longValue();

        int estadoActual = ((Number) original.get(
            "estado_incidente")).intValue();

        boolean coincide =
            categoria == r.categoriaId() &&
            prioridad == r.prioridadId();

        // 3. Aceptar exige conservar la propuesta.
        // Rechazar exige que exista una corrección.
        if (r.aceptada() && !coincide) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Para aceptar debes conservar categoría y prioridad sugeridas"
            );
        }

        if (!r.aceptada() && coincide) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Para rechazar debes modificar categoría o prioridad"
            );
        }

        // 4. Aplicar la clasificación validada.
        jdbc.update("""
            UPDATE incidentes
            SET categoria_id = ?, prioridad_id = ?
            WHERE id = ?
            """,
            r.categoriaId(),
            r.prioridadId(),
            incidenteId
        );

        // 5. Guardar el resultado de la revisión.
        jdbc.update("""
            UPDATE sugerencias_ia
            SET aceptada = ?
            WHERE id = ?
            """,
            r.aceptada(), id
        );

        // 6. Registrar la decisión en la bitácora.
        String comentario;

        if (r.aceptada()) {
            comentario = "Supervisor aceptó la sugerencia IA #" + id
                + ". Categoría y prioridad confirmadas.";
        } else {
            comentario = "Supervisor rechazó la sugerencia IA #" + id
                + ". Clasificación corregida. Categoría: "
                + r.categoriaId() + ", prioridad: "
                + r.prioridadId() + ".";
        }

        jdbc.update("""
            INSERT INTO bitacora (
                incidente_id,
                usuario_id,
                estado_anterior_id,
                estado_nuevo_id,
                comentario
            )
            VALUES (?, ?, ?, ?, ?)
            """,
            incidenteId,
            usuarioId,
            estadoActual,
            estadoActual,
            comentario
        );

        return consultar(id);
    }
}