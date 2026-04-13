package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TurnoRequest;
import com.restobar.lapituca.dto.response.TurnoResponse;
import com.restobar.lapituca.entity.Horario;
import com.restobar.lapituca.entity.Turno;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.HorarioRepository;
import com.restobar.lapituca.repository.TurnoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private TurnoService turnoService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestEsValido() {
        TurnoRequest request = new TurnoRequest("Turno Noche", 3L);
        Horario horario = crearHorario(3L, LocalTime.of(18, 0), LocalTime.of(23, 0));

        when(turnoRepository.existsByNombre("Turno Noche")).thenReturn(false);
        when(horarioRepository.findById(3L)).thenReturn(Optional.of(horario));
        when(turnoRepository.save(any(Turno.class))).thenAnswer(invocation -> {
            Turno t = invocation.getArgument(0);
            t.setId(22L);
            t.setFechaHora_registro(LocalDateTime.of(2026, 3, 1, 8, 0));
            t.setFechaHora_actualizacion(LocalDateTime.of(2026, 3, 1, 8, 30));
            return t;
        });

        TurnoResponse response = turnoService.guardar(request);

        ArgumentCaptor<Turno> captor = ArgumentCaptor.forClass(Turno.class);
        verify(turnoRepository).save(captor.capture());
        Turno guardado = captor.getValue();

        assertEquals("Turno Noche", guardado.getNombre());
        assertEquals(horario, guardado.getHorario());

        assertEquals(22L, response.getId());
        assertEquals("Turno Noche", response.getNombre());
        assertEquals(3L, response.getHorarioResponse().getId());
        assertEquals(LocalTime.of(18, 0), response.getHorarioResponse().getHoraInicio());
        assertEquals(LocalTime.of(23, 0), response.getHorarioResponse().getHoraFin());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        TurnoRequest request = new TurnoRequest("Turno Mañana", 1L);
        when(turnoRepository.existsByNombre("Turno Mañana")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Turno con ese nombre", ex.getMessage());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarNotFound_cuandoHorarioNoExiste() {
        TurnoRequest request = new TurnoRequest("Turno Mañana", 100L);
        when(turnoRepository.existsByNombre("Turno Mañana")).thenReturn(false);
        when(horarioRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Horario con id: 100 no encontrado"));
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTurnosMapeados() {
        Horario horario1 = crearHorario(1L, LocalTime.of(8, 0), LocalTime.of(14, 0));
        Horario horario2 = crearHorario(2L, LocalTime.of(14, 0), LocalTime.of(20, 0));

        Turno turno1 = crearTurno(1L, "Turno Mañana", horario1);
        Turno turno2 = crearTurno(2L, "Turno Tarde", horario2);

        when(turnoRepository.findAll()).thenReturn(List.of(turno1, turno2));

        List<TurnoResponse> resultado = turnoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Turno Mañana", resultado.get(0).getNombre());
        assertEquals(LocalTime.of(8, 0), resultado.get(0).getHorarioResponse().getHoraInicio());
        assertEquals("Turno Tarde", resultado.get(1).getNombre());
        assertEquals(LocalTime.of(20, 0), resultado.get(1).getHorarioResponse().getHoraFin());
    }

    @Test
    void obtenerPorId_deberiaRetornarTurnoResponse_cuandoExiste() {
        Horario horario = crearHorario(7L, LocalTime.of(10, 0), LocalTime.of(18, 0));
        Turno turno = crearTurno(11L, "Turno Full", horario);

        when(turnoRepository.findById(11L)).thenReturn(Optional.of(turno));

        TurnoResponse response = turnoService.obtenerPorId(11L);

        assertEquals(11L, response.getId());
        assertEquals("Turno Full", response.getNombre());
        assertEquals(7L, response.getHorarioResponse().getId());
        assertEquals(LocalTime.of(10, 0), response.getHorarioResponse().getHoraInicio());
        assertNotNull(response.getFechaHora_registro());
        assertNotNull(response.getHorarioResponse().getFechaHora_actualizacion());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(turnoRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Turno con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Turno existente = crearTurno(5L, "Turno viejo", crearHorario(1L, LocalTime.of(7, 0), LocalTime.of(13, 0)));
        Horario nuevoHorario = crearHorario(9L, LocalTime.of(15, 0), LocalTime.of(23, 0));
        TurnoRequest request = new TurnoRequest("Turno Nuevo", 9L);

        when(turnoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(turnoRepository.existsByNombreAndIdNot("Turno Nuevo", 5L)).thenReturn(false);
        when(horarioRepository.findById(9L)).thenReturn(Optional.of(nuevoHorario));
        when(turnoRepository.save(existente)).thenReturn(existente);

        TurnoResponse response = turnoService.actualizar(5L, request);

        assertEquals("Turno Nuevo", existente.getNombre());
        assertEquals(nuevoHorario, existente.getHorario());
        assertEquals(5L, response.getId());
        assertEquals("Turno Nuevo", response.getNombre());
        assertEquals(9L, response.getHorarioResponse().getId());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoTurnoNoExiste() {
        TurnoRequest request = new TurnoRequest("Turno Nuevo", 9L);
        when(turnoRepository.findById(500L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.actualizar(500L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Turno con id: 500 no encontrado", ex.getMessage());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Turno existente = crearTurno(5L, "Turno viejo", crearHorario(1L, LocalTime.of(7, 0), LocalTime.of(13, 0)));
        TurnoRequest request = new TurnoRequest("Turno Duplicado", 2L);

        when(turnoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(turnoRepository.existsByNombreAndIdNot("Turno Duplicado", 5L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.actualizar(5L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        verify(horarioRepository, never()).findById(any());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoHorarioNoExiste() {
        Turno existente = crearTurno(5L, "Turno viejo", crearHorario(1L, LocalTime.of(7, 0), LocalTime.of(13, 0)));
        TurnoRequest request = new TurnoRequest("Turno Editado", 200L);

        when(turnoRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(turnoRepository.existsByNombreAndIdNot("Turno Editado", 5L)).thenReturn(false);
        when(horarioRepository.findById(200L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.actualizar(5L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Horario con id: 200 no encontrado", ex.getMessage());
        verify(turnoRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminarTurno_cuandoExiste() {
        Turno turno = crearTurno(70L, "Turno X", crearHorario(4L, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        when(turnoRepository.findById(70L)).thenReturn(Optional.of(turno));

        turnoService.eliminar(70L);

        verify(turnoRepository).delete(turno);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(turnoRepository.findById(701L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> turnoService.eliminar(701L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Turno con id:701 no encontrado", ex.getMessage());
        verify(turnoRepository, never()).delete(any());
    }

    private Turno crearTurno(Long id, String nombre, Horario horario) {
        Turno turno = new Turno();
        turno.setId(id);
        turno.setNombre(nombre);
        turno.setHorario(horario);
        turno.setFechaHora_registro(LocalDateTime.of(2026, 1, 10, 10, 0));
        turno.setFechaHora_actualizacion(LocalDateTime.of(2026, 1, 11, 10, 30));
        return turno;
    }

    private Horario crearHorario(Long id, LocalTime inicio, LocalTime fin) {
        Horario horario = new Horario();
        horario.setId(id);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horario.setFechaHora_registro(LocalDateTime.of(2026, 1, 1, 6, 0));
        horario.setFechaHora_actualizacion(LocalDateTime.of(2026, 1, 2, 6, 30));
        return horario;
    }
}