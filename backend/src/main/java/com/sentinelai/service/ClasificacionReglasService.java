package com.sentinelai.service;

import com.sentinelai.dto.ClasificacionResponse;
import org.springframework.stereotype.Service;
import java.text.Normalizer;
import java.util.Locale;

@Service
public class ClasificacionReglasService {

    public ClasificacionResponse clasificar(String descripcion) {

        String texto = Normalizer.normalize(
            descripcion.toLowerCase(Locale.ROOT),
            Normalizer.Form.NFD
        ).replaceAll("\\p{M}", "");

        long categoriaId = 6L;
        long prioridadId = 3L;
        String categoria = "Otro";
        String prioridad = "Media";
        String justificacion = "No se identifico una categoria especifica.";

        if (contiene(texto, "incendio", "fuego", "llamas", "humo")) {
            categoriaId = 2L;
            categoria = "Incendio";
            prioridadId = 2L;
            justificacion = "Se detectaron indicios de incendio.";

        } else if (contiene(texto, "choque", "colision", "atropello",
                   "volcamiento", "accidente de transito")) {
            categoriaId = 1L;
            categoria = "Accidente de tr\u00e1nsito";
            prioridadId = 2L;
            justificacion = "Se detectaron indicios de accidente vial.";

        } else if (contiene(texto, "inundacion", "desbordamiento",
                   "deslave", "calle inundada", "desborde")) {
            categoriaId = 3L;
            categoria = "Inundaci\u00f3n";
            prioridadId = 2L;
            justificacion = "Se detectaron indicios de inundacion.";

        } else if (contiene(texto, "arbol caido", "poste caido",
                   "muro caido", "derrumbe", "estructura caida",
                   "poste electrico cayo", "poste cayo",
                   "cayo un poste", "arbol cayo",
                   "cayo un arbol", "arbol derribado",
                   "poste derribado", "muro colapsado")) {
            categoriaId = 4L;
            categoria = "\u00c1rbol o estructura ca\u00edda";
            prioridadId = 3L;
            justificacion = "Se detectaron indicios de una estructura caida.";

        } else if (contiene(texto, "herido", "herida", "lesionado", "lesionada", "desmayo",
                   "infarto", "inconsciente", "no respira", "hemorragia")) {
            categoriaId = 5L;
            categoria = "Emergencia m\u00e9dica";
            prioridadId = 2L;
            justificacion = "Se detectaron indicios de emergencia medica.";
        }

        if (contiene(texto, "herido", "herida", "atrapado", "atrapada", "inconsciente",
                "no respira", "hemorragia", "explosion",
                "infarto", "riesgo de muerte")) {
            prioridadId = 1L;
            justificacion += " Posible riesgo grave para las personas.";
        }

        if (categoriaId == 4L && prioridadId == 3L &&
                contiene(texto, "bloquea", "obstruye", "via cerrada",
                         "impide el paso", "calle bloqueada")) {
            prioridadId = 2L;
            justificacion += " Se detecto una obstruccion de la via.";
        }

        prioridad = switch ((int) prioridadId) {
            case 1 -> "Cr\u00edtica";
            case 2 -> "Alta";
            case 3 -> "Media";
            default -> "Baja";
        };

        return new ClasificacionResponse(
            categoriaId,
            categoria,
            prioridadId,
            prioridad,
            justificacion,
            "REGLAS"
        );
    }

    private boolean contiene(String texto, String... palabras) {
        for (String palabra : palabras) {
            if (texto.contains(palabra)) {
                return true;
            }
        }
        return false;
    }
}
