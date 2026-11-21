package com.sentinelai.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** GET /api/estado -> confirma que la API corre y que PostgreSQL responde. */
@RestController
public class EstadoApiController {

    private final JdbcTemplate jdbc;

    public EstadoApiController(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @GetMapping("/api/estado")
    public Map<String, Object> estado() {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put("api", "Sentinel AI");
        r.put("estado", "OK");
        r.put("baseDeDatos", jdbc.queryForObject("SELECT version()", String.class));
        r.put("tablas", jdbc.queryForObject(
                "SELECT count(*) FROM information_schema.tables WHERE table_schema = 'public'", Integer.class));
        r.put("fecha", LocalDateTime.now().withNano(0).toString());
        return r;
    }
}
