package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.HorarioRequest;
import com.restobar.lapituca.dto.response.HorarioResponse;
import com.restobar.lapituca.entity.Horario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.HorarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HorarioServiceTest {

    @Mock
    private HorarioRepository horarioRepository;

    @InjectMocks
    private HorarioService horarioService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestValido() {
        HorarioRequest request = new HorarioRequest(LocalTime.of(8, 0), LocalTime.of(16, 0));

        when(horarioRepository.existsByHoraInicioAndHoraFin(LocalTime.of(8, 0), LocalTime.of(16, 0))).thenReturn(false);
        when(horarioRepository.save(any(Horario.class))).thenAnswer(invocation -> {
            Horario horario = invocation.getArgument(0);
            horario.setId(10L);
            horario.setFechaHora_registro(LocalDateTime.of(2026, 4, 10, 9, 0));
            horario.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 10, 9, 10));
            return horario;
        });

        HorarioResponse response = horarioService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals(LocalTime.of(8, 0), response.getHoraInicio());
        assertEquals(LocalTime.of(16, 0), response.getHoraFin());
        assertNotNull(response.getFechaHora_registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoHoraInicioIgualHoraFin() {
        HorarioRequest request = new HorarioRequest(LocalTime.of(8, 0), LocalTime.of(8, 0));

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("La hora de inicio y fin no pueden ser iguales", ex.getMessage());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoYaExisteHorario() {
        HorarioRequest request = new HorarioRequest(LocalTime.of(8, 0), LocalTime.of(16, 0));
        when(horarioRepository.existsByHoraInicioAndHoraFin(LocalTime.of(8, 0), LocalTime.of(16, 0))).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un horario con esa hora de inicio y fin", ex.getMessage());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoDuracionSuperaLimiteMismoDia() {
        // 8:00 → 18:00 = 10 horas (>9 → debe fallar)
        HorarioRequest request = new HorarioRequest(LocalTime.of(8, 0), LocalTime.of(18, 0));

        when(horarioRepository.existsByHoraInicioAndHoraFin(any(), any())).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El horario no puede durar más de 9 horas", ex.getMessage());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoDuracionSuperaLimiteCruzandoMedianoche() {
        // 20:00 → 07:00 = 11 horas (>9 → debe fallar SEGURO)
        HorarioRequest request = new HorarioRequest(LocalTime.of(20, 0), LocalTime.of(7, 0));

        when(horarioRepository.existsByHoraInicioAndHoraFin(any(), any())).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El horario no puede durar más de 9 horas", ex.getMessage());
    }

    @Test
    void guardar_deberiaGuardar_cuandoDuracionEsMenorOIgualA9Horas() {
        // 18:00 → 03:00 = 9 horas (válido)
        HorarioRequest request = new HorarioRequest(LocalTime.of(18, 0), LocalTime.of(3, 0));

        when(horarioRepository.existsByHoraInicioAndHoraFin(any(), any())).thenReturn(false);
        when(horarioRepository.save(any(Horario.class))).thenAnswer(i -> i.getArgument(0));

        HorarioResponse response = horarioService.guardar(request);

        assertNotNull(response);
        verify(horarioRepository).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarHorariosMapeados() {
        Horario h1 = crearHorario(1L, LocalTime.of(8, 0), LocalTime.of(16, 0));
        Horario h2 = crearHorario(2L, LocalTime.of(16, 0), LocalTime.of(23, 0));
        when(horarioRepository.findAll()).thenReturn(List.of(h1, h2));

        List<HorarioResponse> response = horarioService.listarTodos();

        assertEquals(2, response.size());
        assertEquals(LocalTime.of(8, 0), response.get(0).getHoraInicio());
        assertEquals(LocalTime.of(23, 0), response.get(1).getHoraFin());
    }

    @Test
    void obtenerPorId_deberiaRetornarHorarioResponse_cuandoExiste() {
        Horario horario = crearHorario(11L, LocalTime.of(9, 0), LocalTime.of(17, 0));
        when(horarioRepository.findById(11L)).thenReturn(Optional.of(horario));

        HorarioResponse response = horarioService.obtenerPorId(11L);

        assertEquals(11L, response.getId());
        assertEquals(LocalTime.of(9, 0), response.getHoraInicio());
        assertEquals(LocalTime.of(17, 0), response.getHoraFin());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(horarioRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Horario con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarYRetornarResponse_cuandoRequestValido() {
        Horario existente = crearHorario(5L, LocalTime.of(8, 0), LocalTime.of(14, 0));
        HorarioRequest request = new HorarioRequest(LocalTime.of(10, 0), LocalTime.of(18, 0));

        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(horarioRepository.existsByHoraInicioAndHoraFinAndIdNot(LocalTime.of(10, 0), LocalTime.of(18, 0), 5L)).thenReturn(false);
        when(horarioRepository.save(existente)).thenReturn(existente);

        HorarioResponse response = horarioService.actualizar(5L, request);

        assertEquals(LocalTime.of(10, 0), existente.getHoraInicio());
        assertEquals(LocalTime.of(18, 0), existente.getHoraFin());
        assertEquals(5L, response.getId());
        assertEquals(LocalTime.of(18, 0), response.getHoraFin());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoHorarioNoExiste() {
        HorarioRequest request = new HorarioRequest(LocalTime.of(10, 0), LocalTime.of(18, 0));
        when(horarioRepository.findById(500L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.actualizar(500L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoHoraInicioIgualHoraFin() {
        Horario existente = crearHorario(5L, LocalTime.of(8, 0), LocalTime.of(14, 0));
        HorarioRequest request = new HorarioRequest(LocalTime.of(10, 0), LocalTime.of(10, 0));

        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.actualizar(5L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("La hora de inicio y fin no pueden ser iguales", ex.getMessage());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoCombinacionDuplicada() {
        Horario existente = crearHorario(5L, LocalTime.of(8, 0), LocalTime.of(14, 0));
        HorarioRequest request = new HorarioRequest(LocalTime.of(10, 0), LocalTime.of(18, 0));

        when(horarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(horarioRepository.existsByHoraInicioAndHoraFinAndIdNot(LocalTime.of(10, 0), LocalTime.of(18, 0), 5L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.actualizar(5L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un horario con esa hora de inicio y fin", ex.getMessage());
        verify(horarioRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaEliminar_cuandoExiste() {
        Horario horario = crearHorario(70L, LocalTime.of(7, 0), LocalTime.of(15, 0));
        when(horarioRepository.findById(70L)).thenReturn(Optional.of(horario));

        horarioService.eliminar(70L);

        verify(horarioRepository).delete(horario);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoNoExiste() {
        when(horarioRepository.findById(701L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> horarioService.eliminar(701L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Horario con id:701 no encontrado", ex.getMessage());
        verify(horarioRepository, never()).delete(any());
    }

    private Horario crearHorario(Long id, LocalTime inicio, LocalTime fin) {
        Horario horario = new Horario();
        horario.setId(id);
        horario.setHoraInicio(inicio);
        horario.setHoraFin(fin);
        horario.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 8, 0));
        horario.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 8, 30));
        return horario;
    }
}