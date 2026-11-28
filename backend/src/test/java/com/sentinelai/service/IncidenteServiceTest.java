package com.sentinelai.service;

import com.sentinelai.dto.IncidenteRequest;
import com.sentinelai.exception.SolicitudInvalidaException;
import com.sentinelai.model.*;
import com.sentinelai.repository.*;
import com.sentinelai.security.UsuarioAutenticado;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IncidenteServiceTest {

    @Mock IncidenteRepository incidentes;
    @Mock CategoriaRepository categorias;
    @Mock PrioridadRepository prioridades;
    @Mock EstadoRepository estados;
    @Mock UsuarioRepository usuarios;
    @Mock BitacoraRepository bitacora;
    @Mock RecursoRepository recursos;
    @InjectMocks IncidenteService service;

    private final UsuarioAutenticado agenteToken = new UsuarioAutenticado(2, "agente@sentinel.test", "Agente Demo", "AGENTE");
    private final Usuario agente = new Usuario(2, "Agente Demo", "agente@sentinel.test", "hash", new Rol(2, "AGENTE"), true);
    private final Estado reportado = new Estado(1, "Reportado", (short) 1);

    private IncidenteRequest solicitud(UUID uuid, Integer categoriaId) {
        return new IncidenteRequest(uuid, "Choque entre dos vehículos en el bulevar", "texto",
                13.6769, -89.2483, "Frente a la gasolinera", categoriaId, null, null);
    }

    @Test
    void registraConEstadoReportadoYBitacora() {
        UUID uuid = UUID.randomUUID();
        when(incidentes.findByUuid(uuid)).thenReturn(Optional.empty());
        when(categorias.findById(1)).thenReturn(Optional.of(new Categoria(1, "Accidente de tránsito", "Choques")));
        when(estados.findByNombreIgnoreCase("Reportado")).thenReturn(Optional.of(reportado));
        when(usuarios.findById(2)).thenReturn(Optional.of(agente));
        when(incidentes.save(any(Incidente.class))).thenAnswer(inv -> {
            Incidente i = inv.getArgument(0);
            i.setId(10);
            return i;
        });

        ResultadoRegistro r = service.registrar(solicitud(uuid, 1), agenteToken);

        assertThat(r.creado()).isTrue();
        assertThat(r.incidente().id()).isEqualTo(10);
        assertThat(r.incidente().estadoNombre()).isEqualTo("Reportado");
        assertThat(r.incidente().origen()).isEqualTo("TEXTO");
        assertThat(r.incidente().reportadoPorId()).isEqualTo(2);
        assertThat(r.incidente().categoriaNombre()).isEqualTo("Accidente de tránsito");

        ArgumentCaptor<Bitacora> captor = ArgumentCaptor.forClass(Bitacora.class);
        verify(bitacora).save(captor.capture());
        assertThat(captor.getValue().getEstadoAnterior()).isNull();
        assertThat(captor.getValue().getEstadoNuevo().getNombre()).isEqualTo("Reportado");
    }

    @Test
    void uuidRepetidoNoDuplicaElIncidente() {
        UUID uuid = UUID.randomUUID();
        Incidente existente = new Incidente();
        existente.setId(5);
        existente.setUuid(uuid);
        existente.setDescripcion("Incidente enviado antes");
        existente.setOrigen("TEXTO");
        existente.setLatitud(new BigDecimal("13.676900"));
        existente.setLongitud(new BigDecimal("-89.248300"));
        existente.setEstado(reportado);
        existente.setReportadoPor(agente);
        existente.setFechaReporte(LocalDateTime.now());
        when(incidentes.findByUuid(uuid)).thenReturn(Optional.of(existente));

        ResultadoRegistro r = service.registrar(solicitud(uuid, null), agenteToken);

        assertThat(r.creado()).isFalse();
        assertThat(r.incidente().id()).isEqualTo(5);
        verify(incidentes, never()).save(any());
        verify(bitacora, never()).save(any());
    }

    @Test
    void categoriaInexistenteEsSolicitudInvalida() {
        UUID uuid = UUID.randomUUID();
        when(incidentes.findByUuid(uuid)).thenReturn(Optional.empty());
        when(categorias.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.registrar(solicitud(uuid, 99), agenteToken))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("categoría");
        verify(incidentes, never()).save(any());
    }
}
