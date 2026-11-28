# Reporte de pruebas - Entrega 2

## Sentinel AI - Integrante C
Responsable: Maria Jose Morales Barillas
Fecha: 27 de noviembre de 2026

## Objetivo
Verificar la clasificacion mediante inteligencia artificial,
el respaldo por reglas y la validacion humana de sugerencias.

## Resultados funcionales

| Prueba | Resultado |
|---|---|
| Clasificacion real con Gemini | Exitosa |
| Categoria, prioridad y justificacion JSON | Exitosa |
| Clasificacion con IA desactivada | Exitosa |
| Persistencia en PostgreSQL | Exitosa |
| Aceptacion de sugerencia | Exitosa |
| Correccion humana de sugerencia | Exitosa |
| Rechazo de validacion por agente (403) | Exitosa |
| Registro de auditoria en bitacora | Exitosa |
| Compilacion de Android | Exitosa |
| Generacion de APK debug | Exitosa |

## Pruebas automatizadas

Comando: mvn test

- Pruebas ejecutadas: 12
- Fallos: 0
- Errores: 0
- Omitidas: 0
- Resultado: BUILD SUCCESS

Se incluyen 10 pruebas del clasificador por reglas
y 2 pruebas existentes del controlador de categorias.

## Evidencia de persistencia

- Incidente 4: sugerencia aceptada por supervisor.
- Incidente 5: sugerencia corregida a categoria 4
  y prioridad 2.
- Ambas decisiones registradas en la bitacora local.

## Coleccion Postman

Archivo: docs/Sentinel_AI.postman_collection.json

Incluye nueve solicitudes adicionales para verificar
los endpoints de inteligencia artificial.

Las pruebas funcionales anteriores se ejecutaron mediante
PowerShell y PostgreSQL. Falta ejecutar la coleccion
completa desde Postman.

## Limitaciones pendientes

- Demostracion funcional de la pantalla Android:
  emulador con problema de autorizacion ADB.
- Integracion definitiva con autenticacion JWT y
  navegacion del equipo.
- Verificacion de cuotas numericas del proyecto Gemini.
- Revision e integracion del Pull Request.

## Repositorio

Rama: feature/entrega2-ia

Pull Request:
https://github.com/camila-01f/sentinel-ai/pull/16
## Actualizacion de integracion - 19:11

- Backend oficial integrado desde main (PR #17).
- Conflicto de Postman resuelto conservando ambas colecciones.
- Identidad del supervisor obtenida del JWT.
- 35 pruebas automatizadas ejecutadas.
- 0 fallos, 0 errores, 0 omitidas.
- BUILD SUCCESS.
- Pendiente: pruebas funcionales IA con JWT y Android.
