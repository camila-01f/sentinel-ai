# Sentinel AI — Gestión inteligente de incidentes

Proyecto integrador de **Desarrollo Móvil + IA** · Universidad Dr. José Matías Delgado · Ciclo 02-2026.

Aplicación móvil para registrar incidentes de emergencia municipal por texto o voz, clasificarlos y priorizarlos con IA, geolocalizarlos, asignar recursos y dar seguimiento a su atención, incluso sin conexión.

## Estructura del repositorio

| Carpeta    | Contenido |
|------------|-----------|
| `android/` | App Android (Kotlin + Jetpack Compose) |
| `backend/` | API REST (Spring Boot 3 + PostgreSQL) |
| `sql/`     | Script del esquema, datos de prueba y DER |
| `docs/`    | Documentación y colección de Postman |

## Requisitos del backend

- Java 17 o superior
- Maven 3.9+ (o IntelliJ IDEA, que lo trae incluido)
- PostgreSQL 14 o superior

## Puesta en marcha

### 1. Crear la base de datos

```bash
psql -U postgres -c "CREATE DATABASE sentinel_ai;"
psql -U postgres -d sentinel_ai -f sql/01_schema.sql
psql -U postgres -d sentinel_ai -f sql/02_datos.sql
```

También se puede hacer desde pgAdmin: crear la base `sentinel_ai`, abrir la herramienta de consultas y ejecutar los dos scripts en orden.

### 2. Configurar la conexión

Por defecto el backend usa `localhost:5432`, usuario `postgres` y contraseña `postgres`. Para otros valores, definir variables de entorno:

```
DB_URL=jdbc:postgresql://localhost:5432/sentinel_ai
DB_USER=postgres
DB_PASSWORD=tu_contraseña
```

### 3. Ejecutar el backend

```bash
cd backend
mvn spring-boot:run
```

### 4. Probar

Importar `docs/Sentinel_AI.postman_collection.json` en Postman y ejecutarla en orden (Runner). Los logins guardan los tokens automáticamente.

Pruebas automáticas del backend:

```bash
cd backend
mvn test
```

## Autenticación

1. `POST /api/auth/login` con `{"correo": "...", "password": "..."}` devuelve un token JWT.
2. Las demás peticiones envían el encabezado `Authorization: Bearer <token>`.
3. El token dura 8 horas (`JWT_EXPIRACION_MINUTOS`). En producción se debe definir la variable `JWT_SECRET` (Base64, mínimo 32 bytes).

Errores: `401` sin token o token inválido, `403` cuando el rol no tiene permiso. Todos los errores responden con el mismo formato JSON (`fecha`, `estado`, `error`, `mensaje` y, en validaciones, `campos`).

## Endpoints

| Método | Endpoint | Acceso | Descripción |
|--------|----------|--------|-------------|
| POST | `/api/auth/login` | Público | Inicia sesión y devuelve el token |
| GET | `/api/auth/yo` | Autenticado | Usuario dueño del token |
| GET | `/api/estado` | Público | Estado de la API y de PostgreSQL |
| GET | `/api/categorias`, `/api/categorias/{id}` | Público | Categorías de incidentes |
| GET | `/api/prioridades`, `/api/estados`, `/api/tipos-recurso` | Público | Catálogos |
| GET | `/api/recursos?disponible=` | Autenticado | Lista recursos (filtro opcional) |
| GET | `/api/recursos/{id}` | Autenticado | Un recurso |
| POST | `/api/recursos` | SUPERVISOR | Crea un recurso |
| PUT | `/api/recursos/{id}` | SUPERVISOR | Edita un recurso |
| DELETE | `/api/recursos/{id}` | SUPERVISOR | Elimina un recurso sin asignaciones (409 si tiene) |
| GET | `/api/incidentes` | Autenticado | Lista incidentes, del más reciente al más antiguo |
| GET | `/api/incidentes/{id}` | Autenticado | Un incidente |
| POST | `/api/incidentes` | Autenticado | Registra un incidente (201). Si el `uuid` ya existe devuelve el existente (200) sin duplicar |
| PUT | `/api/incidentes/{id}` | Autenticado | Edita descripción, referencia, categoría y prioridad |
| DELETE | `/api/incidentes/{id}` | SUPERVISOR | Elimina el incidente y libera sus recursos |

Ejemplo de registro de incidente:

```json
{
  "uuid": "6f1c2a7e-8b3d-4c5e-9a10-1b2c3d4e5f99",
  "descripcion": "Árbol caído bloquea un carril de la calle principal",
  "origen": "TEXTO",
  "latitud": 13.6929,
  "longitud": -89.2182,
  "referencia": "Frente al parque central",
  "categoriaId": 4
}
```

## Usuarios de prueba

| Rol | Correo | Contraseña |
|-----|--------|------------|
| Supervisor | supervisor@sentinel.test | Sentinel2026 |
| Agente | agente@sentinel.test | Sentinel2026 |

Son datos ficticios para pruebas.

## Modelo de datos

11 tablas: `roles`, `usuarios`, `categorias`, `prioridades`, `estados`, `tipos_recurso`, `recursos`, `incidentes`, `sugerencias_ia`, `asignaciones`, `bitacora`.

![DER](sql/DER_Sentinel_AI.png)

## Flujo de trabajo

- `main` protegida: los cambios entran solo por pull request.
- Una rama por funcionalidad: `feature/<nombre>`.
- Cada PR lo revisa el otro integrante.
