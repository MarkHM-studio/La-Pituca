package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.MesaRequest;
import com.restobar.lapituca.dto.response.MesaResponse;
import com.restobar.lapituca.dto.response.MesasOcupadasResponse;
import com.restobar.lapituca.entity.Comprobante;
import com.restobar.lapituca.entity.DetalleMesa;
import com.restobar.lapituca.entity.Grupo;
import com.restobar.lapituca.entity.Mesa;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ComprobanteRepository;
import com.restobar.lapituca.repository.DetalleMesaRepository;
import com.restobar.lapituca.repository.MesaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MesaServiceTest {

    @Mock
    private MesaRepository mesaRepository;

    @Mock
    private DetalleMesaRepository detalleMesaRepository;

    @Mock
    private ComprobanteRepository comprobanteRepository;

    @InjectMocks
    private MesaService mesaService;

    @Test
    void guardar_deberiaGuardarMesaConEstadoDesocupado_cuandoRequestValido() {
        MesaRequest request = new MesaRequest("Mesa VIP 01");

        when(mesaRepository.existsByNombre("Mesa VIP 01")).thenReturn(false);
        when(mesaRepository.save(any(Mesa.class))).thenAnswer(invocation -> {
            Mesa mesa = invocation.getArgument(0);
            mesa.setId(10L);
            mesa.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 10, 0));
            mesa.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 10, 15));
            return mesa;
        });

        MesaResponse response = mesaService.guardar(request);

        ArgumentCaptor<Mesa> captor = ArgumentCaptor.forClass(Mesa.class);
        verify(mesaRepository).save(captor.capture());
        Mesa guardada = captor.getValue();

        assertEquals("Mesa VIP 01", guardada.getNombre());
        assertEquals("DESOCUPADO", guardada.getEstado());

        assertEquals(10L, response.getId());
        assertEquals("Mesa VIP 01", response.getNombre());
        assertEquals("DESOCUPADO", response.getEstado());
        assertNotNull(response.getFechaHora_Registro());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        MesaRequest request = new MesaRequest("Mesa VIP 01");
        when(mesaRepository.existsByNombre("Mesa VIP 01")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Mesa con este nombre", ex.getMessage());
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaExcluirMesasEliminadas() {
        Mesa desocupada = crearMesa(1L, "Mesa A", "DESOCUPADO");
        Mesa ocupada = crearMesa(2L, "Mesa B", "OCUPADO");
        Mesa eliminada = crearMesa(3L, "Mesa C", "ELIMINADO");

        when(mesaRepository.findAll()).thenReturn(List.of(desocupada, ocupada, eliminada));

        List<MesaResponse> resultado = mesaService.listarTodos();

        assertEquals(2, resultado.size());
        assertTrue(resultado.stream().noneMatch(m -> "ELIMINADO".equalsIgnoreCase(m.getEstado())));
        assertEquals("Mesa A", resultado.get(0).getNombre());
        assertEquals("Mesa B", resultado.get(1).getNombre());
    }

    @Test
    void obtenerPorId_deberiaRetornarMesaResponse_cuandoExiste() {
        Mesa mesa = crearMesa(8L, "Mesa Terraza", "DESOCUPADO");
        when(mesaRepository.findById(8L)).thenReturn(Optional.of(mesa));

        MesaResponse response = mesaService.obtenerPorId(8L);

        assertEquals(8L, response.getId());
        assertEquals("Mesa Terraza", response.getNombre());
        assertEquals("DESOCUPADO", response.getEstado());
        assertNotNull(response.getFechaHora_Actualizacion());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(mesaRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Mesa con id: 404 no encontrada", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarNombre_cuandoRequestValido() {
        Mesa mesa = crearMesa(5L, "Mesa Vieja", "DESOCUPADO");
        MesaRequest request = new MesaRequest("Mesa Nueva");

        when(mesaRepository.findById(5L)).thenReturn(Optional.of(mesa));
        when(mesaRepository.existsByNombreAndIdNot("Mesa Nueva", 5L)).thenReturn(false);

        MesaResponse response = mesaService.actualizar(5L, request);

        verify(mesaRepository).save(mesa);
        assertEquals("Mesa Nueva", mesa.getNombre());
        assertEquals(5L, response.getId());
        assertEquals("Mesa Nueva", response.getNombre());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoMesaNoExiste() {
        MesaRequest request = new MesaRequest("Mesa Nueva");
        when(mesaRepository.findById(500L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.actualizar(500L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Mesa mesa = crearMesa(5L, "Mesa Vieja", "DESOCUPADO");
        MesaRequest request = new MesaRequest("Mesa Duplicada");

        when(mesaRepository.findById(5L)).thenReturn(Optional.of(mesa));
        when(mesaRepository.existsByNombreAndIdNot("Mesa Duplicada", 5L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.actualizar(5L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe una Mesa con este nombre", ex.getMessage());
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaCambiarEstadoAEliminado_cuandoNoEstaOcupada() {
        Mesa mesa = crearMesa(9L, "Mesa Patio", "DESOCUPADO");
        when(mesaRepository.findById(9L)).thenReturn(Optional.of(mesa));

        mesaService.eliminar(9L);

        assertEquals("ELIMINADO", mesa.getEstado());
        verify(mesaRepository).save(mesa);
    }

    @Test
    void eliminar_deberiaLanzarErrorNegocio_cuandoMesaEstaOcupada() {
        Mesa mesa = crearMesa(9L, "Mesa Patio", "OCUPADO");
        when(mesaRepository.findById(9L)).thenReturn(Optional.of(mesa));

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.eliminar(9L));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("No se puede eliminar una mesa ocupada", ex.getMessage());
        verify(mesaRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoMesaNoExiste() {
        when(mesaRepository.findById(901L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> mesaService.eliminar(901L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Mesa con id: 901 no encontrada", ex.getMessage());
    }

    @Test
    void obtenerMesasOcupadas_deberiaRetornarSoloMesasOcupadas_conComprobanteYsinComprobante() {
        Mesa ocupadaConComprobante = crearMesa(1L, "Mesa 1", "OCUPADO");
        Mesa ocupadaSinComprobante = crearMesa(2L, "Mesa 2", "OCUPADO");
        Mesa desocupada = crearMesa(3L, "Mesa 3", "DESOCUPADO");

        Grupo grupo1 = crearGrupo(11L);
        Grupo grupo2 = crearGrupo(22L);
        Grupo grupo3 = crearGrupo(33L);

        DetalleMesa d1 = new DetalleMesa(101L, ocupadaConComprobante, grupo1);
        DetalleMesa d2 = new DetalleMesa(102L, ocupadaSinComprobante, grupo2);
        DetalleMesa d3 = new DetalleMesa(103L, desocupada, grupo3);

        Comprobante comprobante = new Comprobante();
        comprobante.setId(500L);
        comprobante.setEstado("ABIERTO");

        when(detalleMesaRepository.findAll()).thenReturn(List.of(d1, d2, d3));
        when(comprobanteRepository.findByGrupo_Id(11L)).thenReturn(Optional.of(comprobante));
        when(comprobanteRepository.findByGrupo_Id(22L)).thenReturn(Optional.empty());

        List<MesasOcupadasResponse> resultado = mesaService.obtenerMesasOcupadas();

        assertEquals(2, resultado.size());

        MesasOcupadasResponse primera = resultado.get(0);
        assertEquals(1L, primera.getMesaId());
        assertEquals(11L, primera.getGrupoId());
        assertEquals(500L, primera.getComprobanteId());
        assertEquals("ABIERTO", primera.getEstadoComprobante());

        MesasOcupadasResponse segunda = resultado.get(1);
        assertEquals(2L, segunda.getMesaId());
        assertEquals(22L, segunda.getGrupoId());
        assertNull(segunda.getComprobanteId());
        assertNull(segunda.getEstadoComprobante());
    }

    private Mesa crearMesa(Long id, String nombre, String estado) {
        Mesa mesa = new Mesa();
        mesa.setId(id);
        mesa.setNombre(nombre);
        mesa.setEstado(estado);
        mesa.setFechaHora_registro(LocalDateTime.of(2026, 4, 1, 9, 0));
        mesa.setFechaHora_actualizacion(LocalDateTime.of(2026, 4, 1, 9, 30));
        return mesa;
    }

    private Grupo crearGrupo(Long id) {
        Grupo grupo = new Grupo();
        grupo.setId(id);
        grupo.setEstado("CONSUMIENDO");
        grupo.setTipoGrupo(1);
        return grupo;
    }
}