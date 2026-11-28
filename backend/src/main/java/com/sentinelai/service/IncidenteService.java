package com.sentinelai.service;

import com.sentinelai.dto.IncidenteActualizacionRequest;
import com.sentinelai.dto.IncidenteDTO;
import com.sentinelai.dto.IncidenteRequest;
import com.sentinelai.exception.RecursoNoEncontradoException;
import com.sentinelai.exception.SolicitudInvalidaException;
import com.sentinelai.model.*;
import com.sentinelai.repository.*;
import com.sentinelai.security.UsuarioAutenticado;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class IncidenteService {

    static final String ESTADO_INICIAL = "Reportado";

    private final IncidenteRepository incidentes;
    private final CategoriaRepository categorias;
    private final PrioridadRepository prioridades;
    private final EstadoRepository estados;
    private final UsuarioRepository usuarios;
    private final BitacoraRepository bitacora;
    private final RecursoRepository recursos;

    public IncidenteService(IncidenteRepository incidentes, CategoriaRepository categorias,
                            PrioridadRepository prioridades, EstadoRepository estados,
                            UsuarioRepository usuarios, BitacoraRepository bitacora,
                            RecursoRepository recursos) {
        this.incidentes = incidentes;
        this.categorias = categorias;
        this.prioridades = prioridades;
        this.estados = estados;
        this.usuarios = usuarios;
        this.bitacora = bitacora;
        this.recursos = recursos;
    }

    @Transactional(readOnly = true)
    public List<IncidenteDTO> listar() {
        return incidentes.findAllByOrderByFechaReporteDesc().stream().map(IncidenteDTO::from).toList();
    }

    @Transactional(readOnly = true)
    public IncidenteDTO obtener(Integer id) {
        return IncidenteDTO.from(buscar(id));
    }

    /**
     * Registra un incidente con estado "Reportado" y deja constancia en la bitácora.
     * Si el uuid ya existe (la app reenvió un registro), devuelve el existente sin duplicarlo.
     */
    public ResultadoRegistro registrar(IncidenteRequest request, UsuarioAutenticado autenticado) {
        Optional<Incidente> existente = incidentes.findByUuid(request.uuid());
        if (existente.isPresent()) {
            return new ResultadoRegistro(IncidenteDTO.from(existente.get()), false);
        }

        Categoria categoria = buscarCategoria(request.categoriaId());
        Prioridad prioridad = buscarPrioridad(request.prioridadId());
        Estado estadoInicial = estados.findByNombreIgnoreCase(ESTADO_INICIAL)
                .orElseThrow(() -> new IllegalStateException("Falta el estado " + ESTADO_INICIAL + " en la base de datos"));
        Usuario reportante = usuarios.findById(autenticado.id())
                .orElseThrow(() -> new SolicitudInvalidaException("El usuario del token ya no existe"));

        LocalDateTime ahora = LocalDateTime.now();
        Incidente incidente = new Incidente();
        incidente.setUuid(request.uuid());
        incidente.setDescripcion(request.descripcion().trim());
        incidente.setOrigen(request.origen().toUpperCase());
        incidente.setLatitud(coordenada(request.latitud()));
        incidente.setLongitud(coordenada(request.longitud()));
        incidente.setReferencia(textoOpcional(request.referencia()));
        incidente.setCategoria(categoria);
        incidente.setPrioridad(prioridad);
        incidente.setEstado(estadoInicial);
        incidente.setReportadoPor(reportante);
        incidente.setFechaReporte(request.fechaReporte() != null ? request.fechaReporte() : ahora);
        incidente.setFechaSincronizacion(ahora);

        Incidente guardado = incidentes.save(incidente);
        bitacora.save(new Bitacora(guardado, reportante, null, estadoInicial,
                "Incidente registrado por " + (incidente.getOrigen().equals("VOZ") ? "voz" : "texto")));

        return new ResultadoRegistro(IncidenteDTO.from(guardado), true);
    }

    public IncidenteDTO actualizar(Integer id, IncidenteActualizacionRequest request) {
        Incidente incidente = buscar(id);
        incidente.setDescripcion(request.descripcion().trim());
        incidente.setReferencia(textoOpcional(request.referencia()));
        incidente.setCategoria(buscarCategoria(request.categoriaId()));
        incidente.setPrioridad(buscarPrioridad(request.prioridadId()));
        return IncidenteDTO.from(incidente);
    }

    /** Elimina el incidente; la base borra en cascada su bitácora, sugerencias y asignaciones. */
    public void eliminar(Integer id) {
        Incidente incidente = buscar(id);
        recursos.liberarPorIncidente(id);
        incidentes.delete(incidente);
    }

    private Incidente buscar(Integer id) {
        return incidentes.findConDetalleById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el incidente con id " + id));
    }

    private Categoria buscarCategoria(Integer id) {
        if (id == null) return null;
        return categorias.findById(id)
                .orElseThrow(() -> new SolicitudInvalidaException("No existe la categoría con id " + id));
    }

    private Prioridad buscarPrioridad(Integer id) {
        if (id == null) return null;
        return prioridades.findById(id)
                .orElseThrow(() -> new SolicitudInvalidaException("No existe la prioridad con id " + id));
    }

    private static BigDecimal coordenada(Double valor) {
        return BigDecimal.valueOf(valor).setScale(6, RoundingMode.HALF_UP);
    }

    private static String textoOpcional(String texto) {
        return (texto == null || texto.isBlank()) ? null : texto.trim();
    }
}
