package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ProductoRequest;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.CategoriaRepository;
import com.restobar.lapituca.repository.MarcaRepository;
import com.restobar.lapituca.repository.ProductoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
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
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @InjectMocks
    private ProductoService productoService;

    @Test
    void guardar_deberiaGuardarConStockCero_cuandoCategoriaPreparadaYStockNulo() {
        ProductoRequest request = new ProductoRequest("Pizza Hawaiana", new BigDecimal("35.50"), null, 1L, null);
        Categoria categoria = crearCategoria(1L, "PLATOS PREPARADOS");

        when(productoRepository.existsByNombre("Pizza Hawaiana")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(99L);
            return p;
        });

        ProductoResponse response = productoService.guardar(request);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).save(captor.capture());
        Producto guardado = captor.getValue();

        assertEquals("Pizza Hawaiana", guardado.getNombre());
        assertEquals(new BigDecimal("35.50"), guardado.getPrecio());
        assertEquals(0, guardado.getStock());
        assertEquals(categoria, guardado.getCategoria());
        assertNull(guardado.getMarca());

        assertEquals(99L, response.getId());
        assertEquals("Pizza Hawaiana", response.getNombre());
        assertEquals(0, response.getStock());
        assertEquals(1L, response.getCategoria().getId());
        assertNull(response.getMarca());
    }

    @Test
    void guardar_deberiaGuardarConMarcaYStock_cuandoCategoriaNoPreparada() {
        ProductoRequest request = new ProductoRequest("Coca Cola 500ml", new BigDecimal("6.00"), 100, 3L, 4L);
        Categoria categoria = crearCategoria(3L, "BEBIDAS");
        Marca marca = crearMarca(4L, "COCA COLA");

        when(productoRepository.existsByNombre("Coca Cola 500ml")).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(marcaRepository.findById(4L)).thenReturn(Optional.of(marca));
        when(productoRepository.save(any(Producto.class))).thenAnswer(invocation -> {
            Producto p = invocation.getArgument(0);
            p.setId(10L);
            return p;
        });

        ProductoResponse response = productoService.guardar(request);

        assertEquals(10L, response.getId());
        assertEquals(100, response.getStock());
        assertEquals("BEBIDAS", response.getCategoria().getNombre());
        assertNotNull(response.getMarca());
        assertEquals("COCA COLA", response.getMarca().getNombre());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoNombreYaExiste() {
        ProductoRequest request = new ProductoRequest("Producto Duplicado", new BigDecimal("5.00"), 8, 3L, null);
        when(productoRepository.existsByNombre("Producto Duplicado")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> productoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un producto con ese nombre", ex.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarNotFound_cuandoCategoriaNoExiste() {
        ProductoRequest request = new ProductoRequest("Producto", new BigDecimal("4.50"), 2, 999L, null);
        when(productoRepository.existsByNombre("Producto")).thenReturn(false);
        when(categoriaRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> productoService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Categoria con id: 999 no encontrada"));
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoCategoriaPreparadaYStockNoEsCero() {
        ProductoRequest request = new ProductoRequest("Lomo Saltado", new BigDecimal("25.00"), 5, 2L, null);
        Categoria categoria = crearCategoria(2L, "COCINA");

        when(productoRepository.existsByNombre("Lomo Saltado")).thenReturn(false);
        when(categoriaRepository.findById(2L)).thenReturn(Optional.of(categoria));

        ApiException ex = assertThrows(ApiException.class, () -> productoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Los productos de categoría 1 o 2 deben registrarse con stock 0", ex.getMessage());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarErrorNegocio_cuandoCategoriaNoPreparadaYStockEsNulo() {
        ProductoRequest request = new ProductoRequest("Agua Mineral", new BigDecimal("3.50"), null, 4L, null);
        Categoria categoria = crearCategoria(4L, "CONSUMO DIRECTO");

        when(productoRepository.existsByNombre("Agua Mineral")).thenReturn(false);
        when(categoriaRepository.findById(4L)).thenReturn(Optional.of(categoria));

        ApiException ex = assertThrows(ApiException.class, () -> productoService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El stock es obligatorio para productos de consumo directo", ex.getMessage());
    }

    @Test
    void guardar_deberiaLanzarNotFound_cuandoMarcaNoExiste() {
        ProductoRequest request = new ProductoRequest("Inca Kola", new BigDecimal("6.00"), 15, 3L, 100L);
        Categoria categoria = crearCategoria(3L, "BEBIDAS");

        when(productoRepository.existsByNombre("Inca Kola")).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(marcaRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> productoService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Marca con id: 100 no encontrada", ex.getMessage());
    }

    @Test
    void listarTodos_deberiaMapearProductosConYsinMarca() {
        Categoria categoria = crearCategoria(3L, "BEBIDAS");
        Marca marca = crearMarca(7L, "PEPSI");

        Producto conMarca = crearProducto(1L, "Pepsi", new BigDecimal("5.00"), 40, categoria, marca);
        Producto sinMarca = crearProducto(2L, "Limonada", new BigDecimal("4.00"), 20, categoria, null);

        when(productoRepository.findAll()).thenReturn(List.of(conMarca, sinMarca));

        List<ProductoResponse> resultado = productoService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("PEPSI", resultado.get(0).getMarca().getNombre());
        assertNull(resultado.get(1).getMarca());
    }

    @Test
    void obtenerPorId_deberiaRetornarProductoResponse_cuandoExiste() {
        Categoria categoria = crearCategoria(5L, "SNACKS");
        Producto producto = crearProducto(50L, "Papas", new BigDecimal("7.00"), 30, categoria, null);

        when(productoRepository.findById(50L)).thenReturn(Optional.of(producto));

        ProductoResponse response = productoService.obtenerPorId(50L);

        assertEquals(50L, response.getId());
        assertEquals("Papas", response.getNombre());
        assertEquals("SNACKS", response.getCategoria().getNombre());
        assertNull(response.getMarca());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(productoRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> productoService.obtenerPorId(404L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Producto con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaActualizarCorrectamente_cuandoRequestValido() {
        Producto existente = crearProducto(77L, "Nombre viejo", new BigDecimal("9.00"), 4, crearCategoria(3L, "BEBIDAS"), null);
        ProductoRequest request = new ProductoRequest("Nombre nuevo", new BigDecimal("10.00"), 8, 3L, 11L);
        Categoria categoria = crearCategoria(3L, "BEBIDAS");
        Marca marca = crearMarca(11L, "KR");

        when(productoRepository.findById(77L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByNombreAndIdNot("Nombre nuevo", 77L)).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(marcaRepository.findById(11L)).thenReturn(Optional.of(marca));
        when(productoRepository.save(existente)).thenReturn(existente);

        ProductoResponse response = productoService.actualizar(77L, request);

        assertEquals("Nombre nuevo", existente.getNombre());
        assertEquals(new BigDecimal("10.00"), existente.getPrecio());
        assertEquals(8, existente.getStock());
        assertEquals(marca, existente.getMarca());

        assertEquals(77L, response.getId());
        assertEquals("Nombre nuevo", response.getNombre());
        assertEquals("KR", response.getMarca().getNombre());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        Producto existente = crearProducto(77L, "Nombre viejo", new BigDecimal("9.00"), 4, crearCategoria(3L, "BEBIDAS"), null);
        ProductoRequest request = new ProductoRequest("Duplicado", new BigDecimal("10.00"), 8, 3L, null);

        when(productoRepository.findById(77L)).thenReturn(Optional.of(existente));
        when(productoRepository.existsByNombreAndIdNot("Duplicado", 77L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> productoService.actualizar(77L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        verify(categoriaRepository, never()).findById(any());
        verify(productoRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarNotFound_cuandoProductoNoExiste() {
        ProductoRequest request = new ProductoRequest("Nombre", new BigDecimal("10.00"), 8, 3L, null);
        when(productoRepository.findById(808L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> productoService.actualizar(808L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Producto con id: 808 no encontrado", ex.getMessage());
    }

    @Test
    void eliminar_deberiaEliminar_cuandoProductoExiste() {
        when(productoRepository.existsById(7L)).thenReturn(true);

        productoService.eliminar(7L);

        verify(productoRepository).deleteById(7L);
    }

    @Test
    void eliminar_deberiaLanzarNotFound_cuandoProductoNoExiste() {
        when(productoRepository.existsById(700L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> productoService.eliminar(700L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Producto con id: 700 no encontrado", ex.getMessage());
        verify(productoRepository, never()).deleteById(any());
    }

    private Producto crearProducto(Long id, String nombre, BigDecimal precio, Integer stock, Categoria categoria, Marca marca) {
        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setPrecio(precio);
        producto.setStock(stock);
        producto.setCategoria(categoria);
        producto.setMarca(marca);
        return producto;
    }

    private Categoria crearCategoria(Long id, String nombre) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre(nombre);
        categoria.setFechaHora_registro(LocalDateTime.of(2026, 1, 10, 8, 30));
        categoria.setFechaHora_actualizacion(LocalDateTime.of(2026, 1, 15, 9, 15));
        return categoria;
    }

    private Marca crearMarca(Long id, String nombre) {
        Marca marca = new Marca();
        marca.setId(id);
        marca.setNombre(nombre);
        marca.setFechaHora_registro(LocalDateTime.of(2026, 2, 1, 10, 0));
        marca.setFechaHora_actualizacion(LocalDateTime.of(2026, 2, 3, 11, 10));
        return marca;
    }
}