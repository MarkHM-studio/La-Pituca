package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.PedidoRequest;
import com.restobar.lapituca.dto.response.PedidoDetalleResponse;
import com.restobar.lapituca.dto.response.PedidoResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PedidoServiceTest {

    @InjectMocks
    private PedidoService pedidoService;

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private ComprobanteRepository comprobanteRepository;
    @Mock
    private TipoEntregaRepository tipoEntregaRepository;
    @Mock
    private DetalleMesaRepository detalleMesaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private GrupoRepository grupoRepository;
    @Mock
    private RecetaRepository recetaRepository;
    @Mock
    private InsumoRepository insumoRepository;

    private Producto producto;
    private Comprobante comprobante;
    private Usuario usuario;
    private TipoEntrega tipoEntrega;
    private Pedido pedido;
    private PedidoRequest pedidoRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        producto = new Producto();
        producto.setId(1L);
        producto.setNombre("Producto Test");
        producto.setPrecio(BigDecimal.valueOf(100));
        producto.setStock(50);
        producto.setCategoria(new Categoria());
        producto.getCategoria().setId(3L); // No preparada

        tipoEntrega = new TipoEntrega();
        tipoEntrega.setId(1L);
        tipoEntrega.setNombre("MESA");

        usuario = new Usuario();
        usuario.setId(1L);
        Rol rol = new Rol();
        rol.setNombre("MOZO");
        usuario.setRol(rol);

        comprobante = new Comprobante();
        comprobante.setId(1L);
        comprobante.setEstado("PENDIENTE");

        pedidoRequest = new PedidoRequest();
        pedidoRequest.setProductoId(1L);
        pedidoRequest.setCantidad(2);
        pedidoRequest.setUsuarioId(1L);
        pedidoRequest.setComprobanteId(1L);
        pedidoRequest.setTipoEntregaId(1L);

        pedido = new Pedido();
        pedido.setId(1L);
        pedido.setCantidad(2);
        pedido.setProducto(producto);
        pedido.setComprobante(comprobante);
        pedido.setTipoEntrega(tipoEntrega);
        pedido.setUsuario(usuario);
        pedido.setPrecio_unitario(producto.getPrecio());
        pedido.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(pedido.getCantidad())));
    }

    // --------------------- GUARDAR ---------------------
    @Test
    void testGuardarPedidoExitoso() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(tipoEntregaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrega));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);

        PedidoDetalleResponse response = pedidoService.guardar(pedidoRequest);

        assertNotNull(response);
        assertEquals(2, response.getCantidad());
        verify(pedidoRepository, times(1)).save(any(Pedido.class));
    }

    @Test
    void testGuardarPedidoConUsuarioNoMozo() {
        usuario.getRol().setNombre("CLIENTE");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        ApiException exception = assertThrows(ApiException.class, () -> pedidoService.guardar(pedidoRequest));
        assertTrue(exception.getMessage().contains("Solo los usuarios con rol MOZO"));
    }

    @Test
    void testGuardarPedidoConComprobantePagado() {
        comprobante.setEstado("PAGADO");
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        ApiException exception = assertThrows(ApiException.class, () -> pedidoService.guardar(pedidoRequest));
        assertTrue(exception.getMessage().contains("No se pueden agregar pedidos a un comprobante pagado"));
    }

    // --------------------- OBTENER ---------------------
    @Test
    void testObtenerPorIdExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        PedidoResponse response = pedidoService.obtenerPorId(1L);

        assertEquals(1L, response.getId());
    }

    @Test
    void testObtenerPorIdNoExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> pedidoService.obtenerPorId(1L));
        assertTrue(ex.getMessage().contains("Pedido con id: 1 no encontrado"));
    }

    @Test
    void testObtenerDetallePorIdExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        PedidoDetalleResponse response = pedidoService.obtenerDetallePorId(1L);

        assertEquals(1L, response.getId());
    }

    // --------------------- LISTAR ---------------------
    @Test
    void testListarTodos() {
        when(pedidoRepository.findAll()).thenReturn(List.of(pedido));
        List<PedidoResponse> responses = pedidoService.listarTodos();

        assertEquals(1, responses.size());
    }

    // --------------------- ELIMINAR ---------------------
    @Test
    void testEliminarPedido() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        pedidoService.eliminar(1L);

        verify(pedidoRepository, times(1)).delete(pedido);
    }

    @Test
    void testEliminarPedidoNoExistente() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.empty());
        ApiException ex = assertThrows(ApiException.class, () -> pedidoService.eliminar(1L));
        assertTrue(ex.getMessage().contains("Pedido con id: 1 no encontrado"));
    }

    // --------------------- OBTENER POR COMPROBANTE ---------------------
    @Test
    void testObtenerPorComprobanteIdExitoso() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(pedidoRepository.findByComprobante_Id(1L)).thenReturn(List.of(pedido));

        List<PedidoResponse> responses = pedidoService.obtenerPorComprobanteId(1L);
        assertEquals(1, responses.size());
    }

    @Test
    void testObtenerPorComprobanteIdNoExistente() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(pedidoRepository.findByComprobante_Id(1L)).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> pedidoService.obtenerPorComprobanteId(1L));
        assertTrue(ex.getMessage().contains("No hay pedidos para este comprobante"));
    }

    @Test
    void testObtenerDetallePorComprobanteIdExitoso() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(pedidoRepository.findByComprobante_Id(1L)).thenReturn(List.of(pedido));

        List<PedidoDetalleResponse> responses = pedidoService.obtenerDetallePorComprobanteId(1L);
        assertEquals(1, responses.size());
    }

    @Test
    void testObtenerDetallePorComprobanteIdNoExistente() {
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(pedidoRepository.findByComprobante_Id(1L)).thenReturn(List.of());

        ApiException ex = assertThrows(ApiException.class, () -> pedidoService.obtenerDetallePorComprobanteId(1L));
        assertTrue(ex.getMessage().contains("No hay pedidos para este comprobante"));
    }

    // --------------------- ACTUALIZAR ---------------------
    @Test
    void testActualizarPedidoExitoso() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(tipoEntregaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrega));
        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedido);
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        PedidoDetalleResponse response = pedidoService.actualizar(1L, pedidoRequest);

        assertEquals(1L, response.getId());
    }

    // --------------------- MARCAR ESTADOS ---------------------
    @Test
    void testMarcarComoListo() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        pedidoService.marcarComoListo(1L);
        assertEquals("LISTO", pedido.getEstado());
    }

    @Test
    void testMarcarComoPreparando() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        pedidoService.marcarComoPreparando(1L);
        assertEquals("PREPARANDO", pedido.getEstado());
    }

    @Test
    void testMarcarComoEntregado() {
        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        pedidoService.marcarComoEntregado(1L);
        assertEquals("ENTREGADO", pedido.getEstado());
    }

    // --------------------- STOCK Y CONVERT ---------------------
    @Test
    void testGuardarPedidoConStockInsuficiente() {
        producto.setStock(1); // menor que cantidad pedida
        pedidoRequest.setCantidad(2);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(tipoEntregaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrega));

        ApiException ex = assertThrows(ApiException.class, () -> pedidoService.guardar(pedidoRequest));
        assertTrue(ex.getMessage().contains("Stock insuficiente"));
    }

    @Test
    void testGuardarPedidoConProductoUnidad() {
        // Categoría tipo “unidad” (para isUnits() = true)
        producto.getCategoria().setNombre("UNIDAD");
        pedidoRequest.setCantidad(5);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));
        when(productoRepository.findById(1L)).thenReturn(Optional.of(producto));
        when(tipoEntregaRepository.findById(1L)).thenReturn(Optional.of(tipoEntrega));

        // Simulamos que al guardar devuelve un pedido con la cantidad que pedimos
        Pedido pedidoGuardado = new Pedido();
        pedidoGuardado.setId(1L);
        pedidoGuardado.setCantidad(pedidoRequest.getCantidad()); // 5
        pedidoGuardado.setProducto(producto);
        pedidoGuardado.setComprobante(comprobante);
        pedidoGuardado.setUsuario(usuario);
        pedidoGuardado.setTipoEntrega(tipoEntrega);
        pedidoGuardado.setPrecio_unitario(producto.getPrecio());
        pedidoGuardado.setSubtotal(producto.getPrecio().multiply(BigDecimal.valueOf(pedidoGuardado.getCantidad())));

        when(pedidoRepository.save(any(Pedido.class))).thenReturn(pedidoGuardado);

        PedidoDetalleResponse response = pedidoService.guardar(pedidoRequest);
        assertNotNull(response);
        assertEquals(5, response.getCantidad());
    }

    // --------------------- DEVOLVER STOCK ---------------------
    @Test
    void testEliminarPedidoDevuelveStock() {
        producto.setStock(10); // stock inicial
        pedido.setCantidad(3);

        when(pedidoRepository.findById(1L)).thenReturn(Optional.of(pedido));
        when(comprobanteRepository.findById(1L)).thenReturn(Optional.of(comprobante));

        pedidoService.eliminar(1L);

        assertEquals(13, producto.getStock()); // stock devuelto
    }
}