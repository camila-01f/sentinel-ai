# Justificacion del servicio IA - Sentinel AI

Entrega 2 - Integrante C
Fecha: 27 de noviembre de 2026

## 1. Servicio seleccionado

Proveedor: Google Gemini API.
Modelo: gemini-3.1-flash-lite.
Integracion: API REST desde Spring Boot.

## 2. Justificacion tecnica

Se selecciono Gemini Flash-Lite por su eficiencia,
disponibilidad de nivel gratuito y capacidad de
generar respuestas estructuradas en JSON.

Permite clasificar descripciones de incidentes
y sugerir categoria, prioridad y justificacion.

## 3. Costos

El nivel gratuito permite realizar pruebas
sin costo dentro de los limites aplicables.

Tarifas del nivel de pago:

| Concepto | Precio por millon de tokens |
| --- | --- |
| Entrada de texto | USD 0.25 |
| Salida | USD 1.50 |

Estimacion para 1000 clasificaciones:

- 500 tokens de entrada por solicitud.
- 150 tokens de salida por solicitud.
- Costo de entrada: USD 0.125.
- Costo de salida: USD 0.225.
- Costo total estimado: USD 0.35.

Es una simulacion presupuestaria, no una
medicion del consumo real.

## 4. Cuotas y limites

Google aplica limites de consumo:

- RPM: solicitudes por minuto.
- TPM: tokens por minuto.
- RPD: solicitudes por dia.

Los limites efectivos dependen del proyecto,
modelo y nivel de servicio.

Las cuotas numericas del proyecto utilizado
no han sido verificadas.

Si el servicio falla o agota su cuota,
el backend utiliza el respaldo por reglas.

## 5. Configuracion segura

Variables de entorno:

- GEMINI_API_KEY
- GEMINI_MODEL

Las credenciales permanecen en el backend.
No deben almacenarse en Android ni GitHub.

Las pruebas utilizan incidentes ficticios.
No deben enviarse datos personales sensibles,
especialmente en el nivel gratuito, cuyos datos
pueden utilizarse para mejorar los servicios
de Google.

## 6. Flujo de clasificacion

1. El backend recibe una descripcion.
2. Consulta el servicio Gemini.
3. Verifica la respuesta estructurada.
4. Aplica reglas si Gemini no responde.
5. Devuelve categoria, prioridad, justificacion
   y fuente IA o REGLAS.
6. La sugerencia se almacena en PostgreSQL.
7. El supervisor acepta o corrige la sugerencia.
8. La decision se registra en bitacora.

## 7. Evidencias verificadas

- Clasificacion real mediante Gemini.
- Respaldo con IA desactivada.
- Persistencia en PostgreSQL.
- Aceptacion y correccion humana.
- Rechazo de validacion por un agente.
- Registro de auditoria.
- 12 pruebas Maven exitosas.
- Compilacion Android y generacion del APK.

## 8. Limitaciones pendientes

El fallback se basa en palabras clave y no
comprende todas las variantes del lenguaje.

La comprobacion actual del rol requiere
integrarse con la autenticacion JWT definitiva.

La navegacion Android requiere coordinacion
con el integrante responsable de la aplicacion.

La prueba en emulador permanece pendiente
por un problema de autorizacion de ADB.

## 9. Referencias oficiales

- https://ai.google.dev/gemini-api/docs/pricing
- https://ai.google.dev/gemini-api/docs/rate-limits
- https://aistudio.google.com/

Consulta: 27 de noviembre de 2026.