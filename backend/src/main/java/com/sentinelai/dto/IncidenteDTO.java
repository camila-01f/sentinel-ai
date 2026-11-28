package com.sentinelai.dto;

import com.sentinelai.model.Incidente;

import java.time.LocalDateTime;

public record IncidenteDTO(
        Integer id,
        String uuid,
        String descripcion,
        String origen,
        Double latitud,
        Double longitud,
        String referencia,
        Integer categoriaId,
        String categoriaNombre,
        Integer prioridadId,
        String prioridadNombre,
        Integer estadoId,
        String estadoNombre,
        Integer reportadoPorId,
        String reportadoPorNombre,
        LocalDateTime fechaReporte,
        LocalDateTime fechaSincronizacion
) {
    public static IncidenteDTO from(Incidente i) {
        return new IncidenteDTO(
                i.getId(),
                i.getUuid() == null ? null : i.getUuid().toString(),
                i.getDescripcion(),
                i.getOrigen(),
                i.getLatitud() == null ? null : i.getLatitud().doubleValue(),
                i.getLongitud() == null ? null : i.getLongitud().doubleValue(),
                i.getReferencia(),
                i.getCategoria() == null ? null : i.getCategoria().getId(),
                i.getCategoria() == null ? null : i.getCategoria().getNombre(),
                i.getPrioridad() == null ? null : i.getPrioridad().getId(),
                i.getPrioridad() == null ? null : i.getPrioridad().getNombre(),
                i.getEstado().getId(),
                i.getEstado().getNombre(),
                i.getReportadoPor().getId(),
                i.getReportadoPor().getNombre(),
                i.getFechaReporte(),
                i.getFechaSincronizacion()
        );
    }
}
