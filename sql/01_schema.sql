-- =====================================================================
-- Sentinel AI - Gestión inteligente de incidentes
-- Script 01: creación del esquema (PostgreSQL 14+)
-- =====================================================================

DROP TABLE IF EXISTS bitacora, asignaciones, sugerencias_ia, incidentes, recursos,
  tipos_recurso, estados, prioridades, categorias, usuarios, roles CASCADE;

-- ---------- Seguridad ----------
CREATE TABLE roles (
  id      SERIAL PRIMARY KEY,
  nombre  VARCHAR(30) NOT NULL UNIQUE
);

CREATE TABLE usuarios (
  id             SERIAL PRIMARY KEY,
  nombre         VARCHAR(100) NOT NULL,
  correo         VARCHAR(120) NOT NULL UNIQUE,
  password_hash  VARCHAR(100) NOT NULL,
  rol_id         INT NOT NULL REFERENCES roles(id),
  activo         BOOLEAN NOT NULL DEFAULT TRUE,
  fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Catálogos ----------
CREATE TABLE categorias (
  id          SERIAL PRIMARY KEY,
  nombre      VARCHAR(60) NOT NULL UNIQUE,
  descripcion VARCHAR(200)
);

CREATE TABLE prioridades (
  id     SERIAL PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE,
  nivel  SMALLINT    NOT NULL UNIQUE CHECK (nivel BETWEEN 1 AND 4),  -- 1 = crítica
  color  CHAR(7)     NOT NULL                                         -- #RRGGBB
);

CREATE TABLE estados (
  id     SERIAL PRIMARY KEY,
  nombre VARCHAR(20) NOT NULL UNIQUE,
  orden  SMALLINT    NOT NULL UNIQUE
);

CREATE TABLE tipos_recurso (
  id     SERIAL PRIMARY KEY,
  nombre VARCHAR(50) NOT NULL UNIQUE
);

-- ---------- Operación ----------
CREATE TABLE recursos (
  id              SERIAL PRIMARY KEY,
  nombre          VARCHAR(80) NOT NULL UNIQUE,
  tipo_recurso_id INT NOT NULL REFERENCES tipos_recurso(id),
  disponible      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE incidentes (
  id                   SERIAL PRIMARY KEY,
  uuid                 UUID NOT NULL UNIQUE,            -- generado en el dispositivo (evita duplicados offline)
  descripcion          TEXT NOT NULL CHECK (char_length(descripcion) >= 10),
  origen               VARCHAR(10) NOT NULL CHECK (origen IN ('TEXTO','VOZ')),
  latitud              NUMERIC(9,6) NOT NULL CHECK (latitud  BETWEEN -90  AND 90),
  longitud             NUMERIC(9,6) NOT NULL CHECK (longitud BETWEEN -180 AND 180),
  referencia           VARCHAR(200),
  categoria_id         INT REFERENCES categorias(id),   -- NULL hasta que se valide
  prioridad_id         INT REFERENCES prioridades(id),
  estado_id            INT NOT NULL REFERENCES estados(id),
  reportado_por        INT NOT NULL REFERENCES usuarios(id),
  fecha_reporte        TIMESTAMP NOT NULL,              -- hora real en que se capturó
  fecha_sincronizacion TIMESTAMP NOT NULL DEFAULT NOW() -- hora en que llegó al servidor
);

CREATE TABLE sugerencias_ia (
  id                    SERIAL PRIMARY KEY,
  incidente_id          INT NOT NULL REFERENCES incidentes(id) ON DELETE CASCADE,
  categoria_sugerida_id INT NOT NULL REFERENCES categorias(id),
  prioridad_sugerida_id INT NOT NULL REFERENCES prioridades(id),
  justificacion         VARCHAR(500),
  fuente                VARCHAR(10) NOT NULL CHECK (fuente IN ('IA','REGLAS')),
  aceptada              BOOLEAN,                        -- NULL = pendiente de revisión
  fecha                 TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE asignaciones (
  id               SERIAL PRIMARY KEY,
  incidente_id     INT NOT NULL REFERENCES incidentes(id) ON DELETE CASCADE,
  recurso_id       INT NOT NULL REFERENCES recursos(id),
  asignado_por     INT NOT NULL REFERENCES usuarios(id),
  fecha_asignacion TIMESTAMP NOT NULL DEFAULT NOW(),
  fecha_liberacion TIMESTAMP                            -- NULL = asignación activa
);

CREATE TABLE bitacora (
  id                 SERIAL PRIMARY KEY,
  incidente_id       INT NOT NULL REFERENCES incidentes(id) ON DELETE CASCADE,
  usuario_id         INT NOT NULL REFERENCES usuarios(id),
  estado_anterior_id INT REFERENCES estados(id),        -- NULL en el primer registro
  estado_nuevo_id    INT NOT NULL REFERENCES estados(id),
  comentario         VARCHAR(500),
  fecha              TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ---------- Índices ----------
CREATE INDEX idx_incidentes_estado    ON incidentes(estado_id);
CREATE INDEX idx_incidentes_prioridad ON incidentes(prioridad_id);
CREATE INDEX idx_bitacora_incidente   ON bitacora(incidente_id);
-- Un recurso solo puede tener una asignación activa a la vez
CREATE UNIQUE INDEX uq_asignacion_activa ON asignaciones(recurso_id) WHERE fecha_liberacion IS NULL;
