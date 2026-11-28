package com.sentinelai.service;

import com.sentinelai.dto.RecursoDTO;
import com.sentinelai.dto.RecursoRequest;
import com.sentinelai.exception.ConflictoException;
import com.sentinelai.exception.SolicitudInvalidaException;
import com.sentinelai.model.Recurso;
import com.sentinelai.model.TipoRecurso;
import com.sentinelai.repository.RecursoRepository;
import com.sentinelai.repository.TipoRecursoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RecursoServiceTest {

    @Mock RecursoRepository recursos;
    @Mock TipoRecursoRepository tipos;
    @InjectMocks RecursoService service;

    private final TipoRecurso ambulancia = new TipoRecurso(1, "Ambulancia");

    @Test
    void creaRecursoDisponiblePorDefecto() {
        when(recursos.existsByNombreIgnoreCase("Ambulancia A-03")).thenReturn(false);
        when(tipos.findById(1)).thenReturn(Optional.of(ambulancia));
        when(recursos.save(any(Recurso.class))).thenAnswer(inv -> inv.getArgument(0));

        RecursoDTO dto = service.crear(new RecursoRequest(" Ambulancia A-03 ", 1, null));

        assertThat(dto.nombre()).isEqualTo("Ambulancia A-03");
        assertThat(dto.tipoNombre()).isEqualTo("Ambulancia");
        assertThat(dto.disponible()).isTrue();
    }

    @Test
    void nombreRepetidoEsConflicto() {
        when(recursos.existsByNombreIgnoreCase("Ambulancia A-01")).thenReturn(true);

        assertThatThrownBy(() -> service.crear(new RecursoRequest("Ambulancia A-01", 1, true)))
                .isInstanceOf(ConflictoException.class);
        verify(recursos, never()).save(any());
    }

    @Test
    void tipoInexistenteEsSolicitudInvalida() {
        when(recursos.existsByNombreIgnoreCase("Grúa G-01")).thenReturn(false);
        when(tipos.findById(99)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.crear(new RecursoRequest("Grúa G-01", 99, true)))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void noSeEliminaUnRecursoConAsignaciones() {
        when(recursos.findConTipoById(1)).thenReturn(Optional.of(new Recurso(1, "Ambulancia A-01", ambulancia, false)));
        when(recursos.contarAsignaciones(1)).thenReturn(1L);

        assertThatThrownBy(() -> service.eliminar(1)).isInstanceOf(ConflictoException.class);
        verify(recursos, never()).delete(any());
    }
}
