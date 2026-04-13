package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.AsignarMesasRequest;
import com.restobar.lapituca.dto.request.ComprobanteRequest;
import com.restobar.lapituca.dto.request.RegistrarVentaRequest;
import com.restobar.lapituca.dto.response.ComprobanteDetalleResponse;
import com.restobar.lapituca.dto.response.ComprobanteListadoResponse;
import com.restobar.lapituca.dto.response.ComprobanteResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
class ComprobanteServiceTest {

    @Mock private ComprobanteRepository comprobanteRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private MesaRepository mesaRepository;
    @Mock private DetalleMesaRepository detalleMesaRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TipoPagoRepository tipoPagoRepository;
    @Mock private TipoBilleteraVirtualRepository tipoBilleteraVirtualRepository;
    @Mock private PedidoRepository pedidoRepository;
    @Mock private MovimientoTipoPagoRepository movimientoTipoPagoRepository;
    @Mock private MovimientoInsumoRepository movimientoInsumoRepository;
    @Mock private RecetaRepository recetaRepository;
    @Mock private SucursalRepository sucursalRepository;
    @Mock private ReservaRepository reservaRepository;
    @Mock private ClienteRepository clienteRepository;

    @InjectMocks
    private ComprobanteService comprobanteService;

    @Test
    void crearComprobante_deberiaCrearComprobanteAbierto_cuandoSucursalExiste() {
        Sucursal sucursal = crearSucursal(1L, "Central");
        ComprobanteRequest request = new ComprobanteRequest(1L);

        when(sucursalRepository.findById(1L)).thenReturn(Optional.of(sucursal));
        when(comprobanteRepository.save(any(Comprobante.class))).thenAnswer(invocation -> {
            Comprobante c = invocation.getArgument(0);
            c.setId(100L);
            c.setFechaHora_apertura(LocalDateTime.of(2026, 4, 12, 15, 0));
            return c;
        });

        ComprobanteResponse response = comprobanteService.crearComprobante(request);

        assertEquals(100L, response.getId());
        assertEquals(new BigDecimal("0"), response.getTotal());
        assertEquals("ABIERTO", response.getEstado());
        assertNull(response.getGrupoResponse());
    }

    @Test
    void crearComprobante_deberiaLanzarNotFound_cuandoSucursalNoExiste() {
        ComprobanteRequest request = new ComprobanteRequest(99L);
        when(sucursalRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> comprobanteService.crearComprobante(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Sucursal con id: 99 no encontrado"));
    }

    @Test
    void listarTodos_deberiaExcluirEliminados() {
        Comprobante abierto = crearComprobante(1L, "ABIERTO", new BigDecimal("20.00"));
        abierto.setSucursal(crearSucursal(10L, "Central"));
        Comprobante eliminado = crearComprobante(2L, "ELIMINADO", new BigDecimal("30.00"));

        when(comprobanteRepository.findAll()).thenReturn(List.of(abierto, eliminado));

        List<ComprobanteListadoResponse> response = comprobanteService.listarTodos();

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals(10L, response.get(0).getSucursalId());
    }

    @Test
    void obtenerDetallePorId_deberiaRetornarDetalle_cuandoComprobanteValido() {
        Comprobante comprobante = crearComprobante(3L, "ABIERTO", new BigDecimal("50.00"));
        comprobante.setSucursal(crearSucursal(7L, "Norte"));

        when(comprobanteRepository.findById(3L)).thenReturn(Optional.of(comprobante));
        when(pedidoRepository.findByComprobante_Id(3L)).thenReturn(List.of());
        when(movimientoTipoPagoRepository.findByComprobante_Id(3L)).thenReturn(List.of());

        ComprobanteDetalleResponse response = comprobanteService.obtenerDetallePorId(3L);

        assertEquals(3L, response.getId());
        assertEquals("ABIERTO", response.getEstado());
        assertNotNull(response.getSucursal());
        assertEquals(7L, response.getSucursal().getId());
    }

    @Test
    void obtenerDetallePorId_deberiaLanzarNotFound_cuandoComprobanteEliminado() {
        Comprobante comprobante = crearComprobante(4L, "ELIMINADO", new BigDecimal("10.00"));
        when(comprobanteRepository.findById(4L)).thenReturn(Optional.of(comprobante));

        ApiException ex = assertThrows(ApiException.class, () -> comprobanteService.obtenerDetallePorId(4L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Comprobante con id: 4 no disponible", ex.getMessage());
    }

    @Test
    void asignarGrupoYMesasSiEsComer_deberiaAsignarGrupoYMesa_cuandoValido() {
        Comprobante comprobante = crearComprobante(9L, "ABIERTO", new BigDecimal("40.00"));
        Pedido pedidoComer = crearPedidoConTipoEntrega("COMER");
        comprobante.setPedidos(List.of(pedidoComer));

        Mesa mesa = new Mesa();
        mesa.setId(2L);
        mesa.setNombre("Mesa 2");
        mesa.setEstado("DESOCUPADO");

        AsignarMesasRequest request = new AsignarMesasRequest(9L, Set.of(2L), "Grupo Familiar");

        when(comprobanteRepository.findById(9L)).thenReturn(Optional.of(comprobante));
        when(grupoRepository.save(any(Grupo.class))).thenAnswer(invocation -> {
            Grupo g = invocation.getArgument(0);
            g.setId(77L);
            return g;
        });
        when(mesaRepository.findById(2L)).thenReturn(Optional.of(mesa));
        when(detalleMesaRepository.findByGrupo_Id(77L)).thenReturn(List.of(new DetalleMesa(1L, mesa, new Grupo(77L, "Grupo Familiar", "ACTIVO", 1, null, null, null, null, null))));

        ComprobanteResponse response = comprobanteService.asignarGrupoYMesasSiEsComer(request);

        assertNotNull(response.getGrupoResponse());
        assertEquals(77L, response.getGrupoResponse().getId());
        assertEquals("OCUPADO", mesa.getEstado());
    }

    @Test
    void asignarGrupoYMesasSiEsComer_deberiaLanzarError_cuandoNoHayPedidos() {
        Comprobante comprobante = crearComprobante(9L, "ABIERTO", new BigDecimal("40.00"));
        comprobante.setPedidos(List.of());
        AsignarMesasRequest request = new AsignarMesasRequest(9L, Set.of(2L), "Grupo Familiar");
        when(comprobanteRepository.findById(9L)).thenReturn(Optional.of(comprobante));

        ApiException ex = assertThrows(ApiException.class, () -> comprobanteService.asignarGrupoYMesasSiEsComer(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("No se puede asignar mesas a un comprobante sin pedidos", ex.getMessage());
    }

    @Test
    void registrarVenta_deberiaLanzarError_cuandoUsuarioNoEsCajero() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Rol rol = new Rol();
        rol.setNombre("MOZO");
        usuario.setRol(rol);

        RegistrarVentaRequest request = new RegistrarVentaRequest(
                1L, 10L, Set.of(1L), List.of(new BigDecimal("20.00")), null,
                "BOLETA", "12345678", null, 1L
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));

        ApiException ex = assertThrows(ApiException.class, () -> comprobanteService.registrarVenta(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Solo los usuarios con rol CAJERO pueden registrar ventas", ex.getMessage());
    }

    @Test
    void registrarVenta_deberiaLanzarError_cuandoMontoInsuficiente() {
        Usuario cajero = crearUsuarioCajero(1L);
        Comprobante comprobante = crearComprobante(10L, "ABIERTO", new BigDecimal("50.00"));

        RegistrarVentaRequest request = new RegistrarVentaRequest(
                1L, 10L, Set.of(1L), List.of(new BigDecimal("30.00")), null,
                "BOLETA", "12345678", null, 1L
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cajero));
        when(comprobanteRepository.findById(10L)).thenReturn(Optional.of(comprobante));

        ApiException ex = assertThrows(ApiException.class, () -> comprobanteService.registrarVenta(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Falta pagar"));
    }

    @Test
    void registrarVenta_deberiaRegistrarPago_cuandoDatosValidos() {
        Usuario cajero = crearUsuarioCajero(1L);
        Comprobante comprobante = crearComprobante(10L, "ABIERTO", new BigDecimal("50.00"));
        comprobante.setPedidos(List.of());

        TipoPago efectivo = new TipoPago();
        efectivo.setId(1L);
        efectivo.setNombre("EFECTIVO");

        RegistrarVentaRequest request = new RegistrarVentaRequest(
                1L, 10L, Set.of(1L), List.of(new BigDecimal("50.00")), null,
                "BOLETA", "12345678", null, 1L
        );

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(cajero));
        when(comprobanteRepository.findById(10L)).thenReturn(Optional.of(comprobante));
        when(tipoPagoRepository.findById(1L)).thenReturn(Optional.of(efectivo));
        when(pedidoRepository.findByComprobante_Id(10L)).thenReturn(List.of());

        String response = comprobanteService.registrarVenta(request);

        assertEquals("Pago realizado correctamente", response);
        assertEquals("PAGADO", comprobante.getEstado());
        verify(movimientoTipoPagoRepository).save(any(MovimientoTipoPago.class));
        verify(comprobanteRepository).save(comprobante);
    }

    private Comprobante crearComprobante(Long id, String estado, BigDecimal total) {
        Comprobante c = new Comprobante();
        c.setId(id);
        c.setEstado(estado);
        c.setTotal(total);
        c.setIGV(new BigDecimal("0.00"));
        c.setSubtotal(total);
        c.setFechaHora_apertura(LocalDateTime.of(2026, 4, 12, 10, 0));
        return c;
    }

    private Sucursal crearSucursal(Long id, String nombre) {
        Sucursal s = new Sucursal();
        s.setId(id);
        s.setNombre(nombre);
        s.setDireccion("Av Test");
        s.setRUC("20123456789");
        return s;
    }

    private Pedido crearPedidoConTipoEntrega(String nombreTipoEntrega) {
        Pedido pedido = new Pedido();
        TipoEntrega tipoEntrega = new TipoEntrega();
        tipoEntrega.setNombre(nombreTipoEntrega);
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setEstado("PENDIENTE");
        return pedido;
    }

    private Usuario crearUsuarioCajero(Long id) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        Rol rol = new Rol();
        rol.setNombre("CAJERO");
        usuario.setRol(rol);
        return usuario;
    }
}
