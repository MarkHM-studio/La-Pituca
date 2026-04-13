package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.EntradaRequest;
import com.restobar.lapituca.dto.response.EntradaResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EntradaServiceTest {

    @Mock private EntradaRepository entradaRepository;
    @Mock private ProductoRepository productoRepository;
    @Mock private InsumoRepository insumoRepository;
    @Mock private ProveedorRepository proveedorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RecetaRepository recetaRepository;

    @InjectMocks private EntradaService entradaService;

    @Test
    void crear_conProductoDirecto_deberiaGuardarEntradaYActualizarStock() {
        EntradaRequest request = new EntradaRequest(
                7L,
                null,
                new BigDecimal("3"),
                "uds",
                new BigDecimal("10.50"),
                1L,
                2L
        );

        Proveedor proveedor = proveedor(1L);
        Usuario usuario = usuario(2L);
        Producto producto = producto(7L, 5, categoria(3L), "Prod A");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));
        when(entradaRepository.save(any(Entrada.class))).thenAnswer(inv -> {
            Entrada e = inv.getArgument(0);
            e.setId(100L);
            e.setFechaHora_registro(LocalDateTime.now());
            return e;
        });

        EntradaResponse response = entradaService.crear(request);

        assertEquals(100L, response.getId());
        assertEquals(7L, response.getProductoId());
        assertNull(response.getInsumoId());
        assertEquals(new BigDecimal("31.50"), response.getCostoTotal());
        assertEquals("UDS", response.getUnidadMedida());

        assertEquals(8, producto.getStock());
        verify(productoRepository).save(producto);
        verify(productoRepository, never()).findByCategoria_IdIn(anyList());
    }

    @Test
    void crear_conInsumo_deberiaConvertirYRecalcularPreparados() {
        EntradaRequest request = new EntradaRequest(
                null,
                9L,
                new BigDecimal("500"),
                "g",
                new BigDecimal("2.00"),
                1L,
                2L
        );

        Proveedor proveedor = proveedor(1L);
        Usuario usuario = usuario(2L);
        Insumo insumo = insumo(9L, new BigDecimal("1.200"), "KG");

        Producto preparado = producto(50L, 0, categoria(1L), "Hamburguesa");
        Receta receta = receta(80L, preparado, insumo, new BigDecimal("250"), "g");

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));
        when(insumoRepository.findById(9L)).thenReturn(Optional.of(insumo));
        when(productoRepository.findByCategoria_IdIn(List.of(1L, 2L))).thenReturn(List.of(preparado));
        when(recetaRepository.findByProducto_IdOrderByIdAsc(50L)).thenReturn(List.of(receta));
        when(entradaRepository.save(any(Entrada.class))).thenAnswer(inv -> {
            Entrada e = inv.getArgument(0);
            e.setId(101L);
            e.setFechaHora_registro(LocalDateTime.now());
            return e;
        });

        EntradaResponse response = entradaService.crear(request);

        assertEquals(101L, response.getId());
        assertEquals(9L, response.getInsumoId());
        assertNull(response.getProductoId());

        // 1.200kg + 500g(0.500kg) = 1.700kg
        assertEquals(0, new BigDecimal("1.700000").compareTo(insumo.getStock()));
        // 1.700kg / 250g(0.250kg) = 6 preparados
        assertEquals(6, preparado.getStock());

        verify(insumoRepository).save(insumo);
        verify(productoRepository, atLeastOnce()).save(preparado);
    }

    @Test
    void crear_deberiaFallarSiSeEnviaProductoEInsumo() {
        EntradaRequest request = new EntradaRequest(
                7L,
                9L,
                new BigDecimal("1"),
                "UD",
                new BigDecimal("5"),
                1L,
                2L
        );

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("solo productoId o solo insumoId"));
    }

    @Test
    void crear_deberiaFallarSiProductoEsCategoriaPreparada() {
        EntradaRequest request = new EntradaRequest(
                7L,
                null,
                new BigDecimal("2"),
                "UNIDADES",
                new BigDecimal("5"),
                1L,
                2L
        );

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto(7L, 10, categoria(1L), "Preparado")));

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("No se permiten entradas directas"));
    }

    @Test
    void crear_deberiaFallarSiUnidadIncompatible() {
        EntradaRequest request = new EntradaRequest(
                null,
                9L,
                new BigDecimal("2"),
                "L",
                new BigDecimal("5"),
                1L,
                2L
        );

        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(insumoRepository.findById(9L)).thenReturn(Optional.of(insumo(9L, new BigDecimal("1"), "KG")));

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Unidad de medida incompatible"));
    }

    @Test
    void listarTodos_deberiaMapearEntradas() {
        Entrada entrada = new Entrada();
        entrada.setId(20L);
        entrada.setCantidad_total(new BigDecimal("4"));
        entrada.setUnidad_medida("UNIDADES");
        entrada.setCosto_unitario(new BigDecimal("3"));
        entrada.setCosto_total(new BigDecimal("12"));
        entrada.setProveedor(proveedor(1L));
        entrada.setUsuario(usuario(2L));
        entrada.setProducto(producto(3L, 10, categoria(3L), "Bebida"));

        when(entradaRepository.findAll()).thenReturn(List.of(entrada));

        List<EntradaResponse> response = entradaService.listarTodos();

        assertEquals(1, response.size());
        assertEquals(20L, response.get(0).getId());
        assertEquals(3L, response.get(0).getProductoId());
        assertEquals(new BigDecimal("12"), response.get(0).getCostoTotal());
    }

    @Test
    void actualizar_desdeProductoAHaciaInsumo_deberiaRevertirYAplicarNuevoImpacto() {
        Entrada existente = new Entrada();
        existente.setId(30L);
        existente.setCantidad_total(new BigDecimal("2"));
        existente.setUnidad_medida("UD");
        Producto productoAnterior = producto(5L, 10, categoria(3L), "Snack");
        existente.setProducto(productoAnterior);

        EntradaRequest request = new EntradaRequest(
                null,
                9L,
                new BigDecimal("1000"),
                "g",
                new BigDecimal("4.5"),
                1L,
                2L
        );

        Insumo insumo = insumo(9L, new BigDecimal("2.0"), "KG");
        Producto preparado = producto(50L, 0, categoria(2L), "Pizza");
        Receta receta = receta(1L, preparado, insumo, new BigDecimal("500"), "g");

        when(entradaRepository.findById(30L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(insumoRepository.findById(9L)).thenReturn(Optional.of(insumo));
        when(productoRepository.findByCategoria_IdIn(List.of(1L, 2L))).thenReturn(List.of(preparado));
        when(recetaRepository.findByProducto_IdOrderByIdAsc(50L)).thenReturn(List.of(receta));
        when(entradaRepository.save(any(Entrada.class))).thenAnswer(inv -> inv.getArgument(0));

        EntradaResponse response = entradaService.actualizar(30L, request);

        // revertir producto anterior: 10 - 2 = 8
        assertEquals(8, productoAnterior.getStock());
        // aplicar nueva entrada a insumo: 2kg + 1kg = 3kg
        assertEquals(0, new BigDecimal("3.000000").compareTo(insumo.getStock()));
        assertEquals(6, preparado.getStock()); // 3kg / 0.5kg
        assertEquals(9L, response.getInsumoId());

        verify(productoRepository).save(productoAnterior);
        verify(insumoRepository).save(insumo);
    }

    @Test
    void actualizar_deberiaFallarSiReversionDejaStockNegativoEnInsumo() {
        Entrada existente = new Entrada();
        existente.setId(40L);
        existente.setCantidad_total(new BigDecimal("2"));
        existente.setUnidad_medida("KG");
        Insumo insumoAnterior = insumo(11L, new BigDecimal("1.5"), "KG");
        existente.setInsumo(insumoAnterior);

        EntradaRequest request = new EntradaRequest(
                null,
                11L,
                new BigDecimal("1"),
                "KG",
                new BigDecimal("10"),
                1L,
                2L
        );

        when(entradaRepository.findById(40L)).thenReturn(Optional.of(existente));

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.actualizar(40L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("stock negativo en insumo"));
        verify(entradaRepository, never()).save(any(Entrada.class));
    }

    @Test
    void actualizar_deberiaFallarSiNoExisteEntrada() {
        EntradaRequest request = new EntradaRequest(
                3L,
                null,
                new BigDecimal("1"),
                "UD",
                new BigDecimal("10"),
                1L,
                2L
        );

        when(entradaRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.actualizar(99L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void actualizar_recalculoConRecetaInvalida_deberiaFallar() {
        Entrada existente = new Entrada();
        existente.setId(41L);
        existente.setCantidad_total(new BigDecimal("1"));
        existente.setUnidad_medida("KG");
        existente.setInsumo(insumo(12L, new BigDecimal("5"), "KG"));

        EntradaRequest request = new EntradaRequest(
                null,
                12L,
                new BigDecimal("1"),
                "KG",
                new BigDecimal("10"),
                1L,
                2L
        );

        Insumo insumo = insumo(12L, new BigDecimal("5"), "KG");
        Producto preparado = producto(60L, 0, categoria(1L), "Salsa");
        Receta recetaInvalida = receta(10L, preparado, insumo, BigDecimal.ZERO, "KG");

        when(entradaRepository.findById(41L)).thenReturn(Optional.of(existente));
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor(1L)));
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario(2L)));
        when(insumoRepository.findById(12L)).thenReturn(Optional.of(insumo));
        when(productoRepository.findByCategoria_IdIn(List.of(1L, 2L))).thenReturn(List.of(preparado));
        when(recetaRepository.findByProducto_IdOrderByIdAsc(60L)).thenReturn(List.of(recetaInvalida));

        ApiException ex = assertThrows(ApiException.class, () -> entradaService.actualizar(41L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Receta inválida"));
    }

    private Proveedor proveedor(Long id) {
        Proveedor p = new Proveedor();
        p.setId(id);
        return p;
    }

    private Usuario usuario(Long id) {
        Usuario u = new Usuario();
        u.setId(id);
        return u;
    }

    private Categoria categoria(Long id) {
        Categoria c = new Categoria();
        c.setId(id);
        return c;
    }

    private Producto producto(Long id, Integer stock, Categoria categoria, String nombre) {
        Producto p = new Producto();
        p.setId(id);
        p.setStock(stock);
        p.setCategoria(categoria);
        p.setNombre(nombre);
        return p;
    }

    private Insumo insumo(Long id, BigDecimal stock, String unidad) {
        Insumo i = new Insumo();
        i.setId(id);
        i.setStock(stock);
        i.setUnidad_medida(unidad);
        return i;
    }

    private Receta receta(Long id, Producto producto, Insumo insumo, BigDecimal cantidad, String unidad) {
        Receta r = new Receta();
        r.setId(id);
        r.setProducto(producto);
        r.setInsumo(insumo);
        r.setCantidad(cantidad);
        r.setUnidad_medida(unidad);
        return r;
    }
}