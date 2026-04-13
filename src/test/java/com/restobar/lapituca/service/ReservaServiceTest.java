package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ReservaRequest;
import com.restobar.lapituca.dto.response.MesasDisponiblesResponse;
import com.restobar.lapituca.dto.response.ReservaResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    @Mock private ReservaRepository reservaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private MesaRepository mesaRepository;
    @Mock private DetalleMesaRepository detalleMesaRepository;
    @Mock private ComprobanteRepository comprobanteRepository;

    @InjectMocks private ReservaService reservaService;

    @Test
    void crear_deberiaCrearReservaYActualizarEstados() {
        ReservaRequest request = new ReservaRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                4,
                10L,
                Set.of(1L, 2L),
                20L
        );

        Usuario usuario = usuario(10L, "cliente");
        Sucursal sucursal = sucursal(20L);
        Mesa mesa1 = mesa(1L, "M1");
        Mesa mesa2 = mesa(2L, "M2");

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(sucursalRepository.findById(20L)).thenReturn(Optional.of(sucursal));
        when(mesaRepository.findMesasForUpdate(Set.of(1L, 2L))).thenReturn(List.of(mesa1, mesa2));
        when(reservaRepository.findReservasSolapadas(any(), any(), any(), anySet())).thenReturn(List.of());
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(inv -> {
            Grupo g = inv.getArgument(0);
            g.setId(99L);
            return g;
        });
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> {
            Reserva r = inv.getArgument(0);
            r.setId(55L);
            return r;
        });

        ReservaResponse response = reservaService.crear(request);

        assertEquals(55L, response.getId());
        assertEquals("ESPERANDO PAGO", response.getEstado());
        assertEquals(10L, response.getUsuarioId());
        assertEquals(99L, response.getGrupoId());

        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
        verify(detalleMesaRepository).saveAll(anyList());
        verify(comprobanteRepository).save(any(Comprobante.class));
        verify(reservaRepository).save(any(Reserva.class));
    }

    @Test
    void crear_deberiaFallarSiHayReservasSolapadas() {
        ReservaRequest request = new ReservaRequest(
                LocalDate.now().plusDays(1),
                LocalTime.of(18, 0),
                2,
                10L,
                Set.of(1L),
                20L
        );

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, "cliente")));
        when(sucursalRepository.findById(20L)).thenReturn(Optional.of(sucursal(20L)));
        when(mesaRepository.findMesasForUpdate(Set.of(1L))).thenReturn(List.of(mesa(1L, "M1")));
        when(reservaRepository.findReservasSolapadas(any(), any(), any(), anySet())).thenReturn(List.of(new Reserva()));

        ApiException ex = assertThrows(ApiException.class, () -> reservaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("ya están reservadas"));
    }

    @Test
    void verMesasDisponibles_deberiaMapearOcupacion() {
        LocalDate fecha = LocalDate.now().plusDays(1);
        LocalTime hora = LocalTime.of(19, 0);

        Mesa m1 = mesa(1L, "M1");
        Mesa m2 = mesa(2L, "M2");

        Reserva reserva = new Reserva();
        Grupo grupo = new Grupo();
        DetalleMesa detalleMesa = new DetalleMesa();
        detalleMesa.setMesa(m1);
        detalleMesa.setGrupo(grupo);
        grupo.setDetalleMesas(List.of(detalleMesa));
        reserva.setGrupo(grupo);

        when(mesaRepository.findAll()).thenReturn(List.of(m1, m2));
        when(reservaRepository.findReservasEnRango(eq(fecha), eq(hora), eq(hora.plusHours(1))))
                .thenReturn(List.of(reserva));

        List<MesasDisponiblesResponse> response = reservaService.verMesasDisponibles(fecha, hora);

        assertEquals(2, response.size());
        assertTrue(response.stream().anyMatch(r -> r.getMesaId().equals(1L) && r.isOcupada()));
        assertTrue(response.stream().anyMatch(r -> r.getMesaId().equals(2L) && !r.isOcupada()));

        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void actualizar_deberiaActualizarReservaYMesas() {
        Long reservaId = 5L;
        ReservaRequest request = new ReservaRequest(
                LocalDate.now().plusDays(2),
                LocalTime.of(20, 0),
                3,
                20L,
                Set.of(10L),
                99L
        );

        Grupo grupo = new Grupo();
        grupo.setId(44L);

        Reserva reserva = new Reserva();
        reserva.setId(reservaId);
        reserva.setEstado("PAGADO");
        reserva.setGrupo(grupo);
        reserva.setUsuario(usuario(10L, "old"));

        when(reservaRepository.findById(reservaId)).thenReturn(Optional.of(reserva));
        when(usuarioRepository.findById(20L)).thenReturn(Optional.of(usuario(20L, "nuevo")));
        when(reservaRepository.findReservasSolapadas(any(), any(), any(), anySet())).thenReturn(List.of(reserva));
        when(mesaRepository.findMesasForUpdate(Set.of(10L))).thenReturn(List.of(mesa(10L, "M10")));
        when(detalleMesaRepository.findByGrupo_Id(44L)).thenReturn(List.of(new DetalleMesa()));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse response = reservaService.actualizar(reservaId, request);

        assertEquals(request.getFechaReserva(), response.getFechaReserva());
        assertEquals(request.getHoraReserva(), response.getHoraReserva());
        assertEquals(20L, response.getUsuarioId());

        verify(detalleMesaRepository).deleteAll(anyList());
        verify(detalleMesaRepository).saveAll(anyList());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void listar_deberiaRetornarReservasMapeadas() {
        Reserva reserva = reservaConRelacion(1L, 10L, "cliente");
        when(reservaRepository.findAll()).thenReturn(List.of(reserva));

        List<ReservaResponse> response = reservaService.listar();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(10L, response.get(0).getUsuarioId());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void obtenerPorId_deberiaLanzarSiNoExiste() {
        when(reservaRepository.findById(123L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> reservaService.obtenerPorId(123L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void listarPorUsername_deberiaFiltrarPorUsuario() {
        Usuario usuario = usuario(10L, "cliente");
        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));

        Reserva propia = reservaConRelacion(1L, 10L, "cliente");
        Reserva ajena = reservaConRelacion(2L, 11L, "otro");
        when(reservaRepository.findAll()).thenReturn(List.of(propia, ajena));

        List<ReservaResponse> response = reservaService.listarPorUsername("cliente");

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void obtenerPorIdParaUsername_deberiaFallarSiUsuarioNoEsPropietario() {
        Usuario usuario = usuario(10L, "cliente");
        Reserva reserva = reservaConRelacion(1L, 11L, "otro");

        when(usuarioRepository.findByUsername("cliente")).thenReturn(Optional.of(usuario));
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class,
                () -> reservaService.obtenerPorIdParaUsername(1L, "cliente"));

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void cancelar_deberiaGuardarEstadoCancelado() {
        Reserva reserva = reservaConRelacion(1L, 10L, "cliente");
        reserva.setEstado("PAGADO");

        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        reservaService.cancelar(1L, "cliente", true);

        ArgumentCaptor<Reserva> captor = ArgumentCaptor.forClass(Reserva.class);
        verify(reservaRepository).save(captor.capture());
        assertEquals("CANCELADO", captor.getValue().getEstado());
    }

    @Test
    void cancelar_deberiaFallarSiClienteIntentaCancelarReservaAjena() {
        Reserva reserva = reservaConRelacion(1L, 10L, "cliente");
        reserva.setEstado("PAGADO");
        when(reservaRepository.findById(1L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class,
                () -> reservaService.cancelar(1L, "otro", true));

        assertEquals(ErrorCode.FORBIDDEN, ex.getErrorCode());
    }

    @Test
    void verificarReserva_deberiaMarcarNoShowSiPasoTolerancia() {
        Reserva reserva = reservaConRelacion(2L, 10L, "cliente");
        reserva.setEstado("PAGADO");
        reserva.setFecha_reserva(LocalDate.now().minusDays(1));
        reserva.setHora_reserva(LocalTime.of(20, 0));

        when(reservaRepository.findById(2L)).thenReturn(Optional.of(reserva));

        ApiException ex = assertThrows(ApiException.class,
                () -> reservaService.verificarReserva(2L, "recep"));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("NO_SHOW", reserva.getEstado());
        verify(reservaRepository).save(reserva);
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    @Test
    void verificarReserva_deberiaVerificarCorrectamente() {
        Reserva reserva = reservaConRelacion(3L, 10L, "cliente");
        reserva.setEstado("PAGADO");
        reserva.setFecha_reserva(LocalDate.now());
        reserva.setHora_reserva(LocalTime.now().plusMinutes(5));

        Usuario recepcionista = usuario(99L, "recep");

        when(reservaRepository.findById(3L)).thenReturn(Optional.of(reserva));
        when(usuarioRepository.findByUsername("recep")).thenReturn(Optional.of(recepcionista));
        when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

        ReservaResponse response = reservaService.verificarReserva(3L, "recep");

        assertEquals(99L, response.getUsuarioVerificadorId());
        assertNotNull(response.getFechaVerificacionReserva());
        verify(reservaRepository).marcarReservasExpiradas();
        verify(reservaRepository).marcarReservasNoShow(any(LocalTime.class));
    }

    private Usuario usuario(Long id, String username) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        return u;
    }

    private Sucursal sucursal(Long id) {
        Sucursal s = new Sucursal();
        s.setId(id);
        return s;
    }

    private Mesa mesa(Long id, String nombre) {
        Mesa m = new Mesa();
        m.setId(id);
        m.setNombre(nombre);
        return m;
    }

    private Reserva reservaConRelacion(Long id, Long usuarioId, String username) {
        Reserva reserva = new Reserva();
        reserva.setId(id);
        reserva.setFecha_reserva(LocalDate.now().plusDays(1));
        reserva.setHora_reserva(LocalTime.of(18, 0));
        reserva.setNum_personas(2);
        reserva.setEstado("PAGADO");
        reserva.setUsuario(usuario(usuarioId, username));

        Grupo grupo = new Grupo();
        grupo.setId(100L + id);

        Mesa mesa = mesa(200L + id, "M" + id);
        DetalleMesa detalle = new DetalleMesa();
        detalle.setMesa(mesa);
        detalle.setGrupo(grupo);
        grupo.setDetalleMesas(List.of(detalle));
        reserva.setGrupo(grupo);

        Transaccion t1 = new Transaccion();
        t1.setId(1000L + id);
        t1.setFechaActualizacion(LocalDateTime.now().minusMinutes(2));
        Transaccion t2 = new Transaccion();
        t2.setId(2000L + id);
        t2.setFechaActualizacion(LocalDateTime.now());
        reserva.setTransacciones(List.of(t1, t2));

        return reserva;
    }
}
