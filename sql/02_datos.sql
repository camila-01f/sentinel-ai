-- =====================================================================
-- Sentinel AI - Script 02: datos iniciales de prueba (no sensibles)
-- Contraseña de los usuarios de prueba: Sentinel2026
-- =====================================================================

INSERT INTO roles (nombre) VALUES ('SUPERVISOR'), ('AGENTE');

INSERT INTO usuarios (nombre, correo, password_hash, rol_id) VALUES
 ('Supervisor Demo', 'supervisor@sentinel.test', '$2b$10$65YFeDotz1maILu4mnAaU.b15s6SykVDOnlzPTQSlGxdiNtuOUTqu', 1),
 ('Agente Demo',     'agente@sentinel.test',     '$2b$10$65YFeDotz1maILu4mnAaU.b15s6SykVDOnlzPTQSlGxdiNtuOUTqu', 2);

INSERT INTO categorias (nombre, descripcion) VALUES
 ('Accidente de tránsito',        'Choques, atropellos y vuelcos'),
 ('Incendio',                     'Incendios estructurales, vehiculares o de maleza'),
 ('Inundación',                   'Desbordes, calles anegadas y deslaves por lluvia'),
 ('Árbol o estructura caída',     'Árboles, postes, muros o rótulos caídos'),
 ('Emergencia médica',            'Personas lesionadas o con malestar grave en vía pública'),
 ('Otro',                         'Incidentes que no encajan en otra categoría');

INSERT INTO prioridades (nombre, nivel, color) VALUES
 ('Crítica', 1, '#C0392B'), ('Alta', 2, '#E67E22'), ('Media', 3, '#F1C40F'), ('Baja', 4, '#27AE60');

INSERT INTO estados (nombre, orden) VALUES
 ('Reportado', 1), ('Asignado', 2), ('En atención', 3), ('Resuelto', 4), ('Cerrado', 5);

INSERT INTO tipos_recurso (nombre) VALUES
 ('Ambulancia'), ('Unidad de bomberos'), ('Cuadrilla municipal'), ('Patrulla');

INSERT INTO recursos (nombre, tipo_recurso_id) VALUES
 ('Ambulancia A-01', 1), ('Ambulancia A-02', 1),
 ('Bomberos B-01', 2),
 ('Cuadrilla C-01', 3), ('Cuadrilla C-02', 3),
 ('Patrulla P-01', 4);

-- Incidente de ejemplo con su sugerencia, asignación y bitácora
INSERT INTO incidentes (uuid, descripcion, origen, latitud, longitud, referencia,
                        categoria_id, prioridad_id, estado_id, reportado_por, fecha_reporte)
VALUES ('6f1c2a7e-8b3d-4c5e-9a10-1b2c3d4e5f60',
        'Choque entre dos vehículos en el bulevar, hay dos personas heridas',
        'TEXTO', 13.676900, -89.248300, 'Frente a la gasolinera', 1, 1, 2, 2, NOW() - INTERVAL '30 minutes');

INSERT INTO sugerencias_ia (incidente_id, categoria_sugerida_id, prioridad_sugerida_id, justificacion, fuente, aceptada)
VALUES (1, 1, 1, 'Colisión vehicular con personas heridas', 'IA', TRUE);

UPDATE recursos SET disponible = FALSE WHERE id = 1;
INSERT INTO asignaciones (incidente_id, recurso_id, asignado_por) VALUES (1, 1, 1);

INSERT INTO bitacora (incidente_id, usuario_id, estado_anterior_id, estado_nuevo_id, comentario) VALUES
 (1, 2, NULL, 1, 'Incidente registrado'),
 (1, 1, 1,    2, 'Se asigna Ambulancia A-01');
