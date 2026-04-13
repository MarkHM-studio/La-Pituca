package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.RecetaRequest;
import com.restobar.lapituca.dto.response.RecetaResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Insumo;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.entity.Receta;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.InsumoRepository;
import com.restobar.lapituca.repository.ProductoRepository;
import com.restobar.lapituca.repository.RecetaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecetaServiceTest {

    @Mock
    private RecetaRepository recetaRepository;

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private InsumoRepository insumoRepository;

    @InjectMocks
    private RecetaService recetaService;

    @Test
    void crear_deberiaGuardarYRetornarRecetas_cuandoRequestEsValido() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        Insumo harina = crearInsumo(1L, "Harina");
        Insumo queso = crearInsumo(2L, "Queso");

        RecetaRequest request = new RecetaRequest(
                7L,
                List.of(1L, 2L),
                List.of(new BigDecimal("0.50"), new BigDecimal("0.20")),
                List.of(" kg ", "uNd")
        );

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));
        when(recetaRepository.existsByProducto_Id(7L)).thenReturn(false);
        when(insumoRepository.findById(1L)).thenReturn(Optional.of(harina));
        when(insumoRepository.findById(2L)).thenReturn(Optional.of(queso));

        AtomicLong idGenerator = new AtomicLong(100L);
        when(recetaRepository.save(any(Receta.class))).thenAnswer(invocation -> {
            Receta receta = invocation.getArgument(0);
            receta.setId(idGenerator.getAndIncrement());
            return receta;
        });

        List<RecetaResponse> response = recetaService.crear(request);

        assertEquals(2, response.size());
        assertEquals(7L, response.get(0).getProductoId());
        assertEquals("Pizza Especial", response.get(0).getProductoNombre());
        assertEquals("KG", response.get(0).getUnidadMedida());
        assertEquals("UND", response.get(1).getUnidadMedida());
        verify(recetaRepository, org.mockito.Mockito.times(2)).save(any(Receta.class));
    }

    @Test
    void crear_deberiaLanzarError_cuandoProductoNoExiste() {
        RecetaRequest request = new RecetaRequest(999L, List.of(1L), List.of(new BigDecimal("1.00")), List.of("KG"));
        when(productoRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Producto con id: 999 no encontrado", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoProductoNoEsCategoriaPreparada() {
        Producto producto = crearProductoPreparado(8L, "Gaseosa", 3L);
        RecetaRequest request = new RecetaRequest(8L, List.of(1L), List.of(new BigDecimal("1.00")), List.of("UND"));

        when(productoRepository.findById(8L)).thenReturn(Optional.of(producto));

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Solo se aceptan recetas para productos de categoría 1 o 2", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoProductoYaTieneReceta() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        RecetaRequest request = new RecetaRequest(7L, List.of(1L), List.of(new BigDecimal("1.00")), List.of("KG"));

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));
        when(recetaRepository.existsByProducto_Id(7L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El producto ya tiene receta. Usa actualizar", ex.getMessage());
        verify(recetaRepository, never()).save(any());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoListasTienenDistintoTamano() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        RecetaRequest request = new RecetaRequest(7L, List.of(1L, 2L), List.of(new BigDecimal("1.00")), List.of("KG", "UND"));

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("insumosId, cantidades y unidadesMedida deben tener igual tamaño", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoInsumosEstaVacio() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        RecetaRequest request = new RecetaRequest(7L, List.of(), List.of(), List.of());

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Debe registrar al menos un insumo", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoInsumoRepetido() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        RecetaRequest request = new RecetaRequest(7L, List.of(1L, 1L), List.of(new BigDecimal("1.00"), new BigDecimal("2.00")), List.of("KG", "KG"));

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("No se puede repetir el mismo insumo en una receta", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarNotFound_cuandoInsumoNoExiste() {
        Producto producto = crearProductoPreparado(7L, "Pizza Especial", 1L);
        RecetaRequest request = new RecetaRequest(7L, List.of(99L), List.of(new BigDecimal("1.00")), List.of("KG"));

        when(productoRepository.findById(7L)).thenReturn(Optional.of(producto));
        when(recetaRepository.existsByProducto_Id(7L)).thenReturn(false);
        when(insumoRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.crear(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Insumo con id: 99 no encontrado", ex.getMessage());
    }

    @Test
    void actualizar_deberiaReemplazarReceta_cuandoRequestValido() {
        Producto producto = crearProductoPreparado(5L, "Lomo Saltado", 2L);
        Insumo carne = crearInsumo(10L, "Carne");
        Insumo otro = crearInsumo(11L, "Otro");
        RecetaRequest request = new RecetaRequest(
                5L,
                List.of(10L, 11L),
                List.of(new BigDecimal("0.30"), new BigDecimal("0.20")),
                List.of("kg", "und")
        );

        when(productoRepository.findById(5L)).thenReturn(Optional.of(producto));
        when(insumoRepository.findById(11L)).thenReturn(Optional.of(otro));
        when(insumoRepository.findById(10L)).thenReturn(Optional.of(carne));
        when(recetaRepository.save(any(Receta.class))).thenAnswer(invocation -> {
            Receta receta = invocation.getArgument(0);
            receta.setId(501L);
            return receta;
        });

        List<RecetaResponse> response = recetaService.actualizar(5L, request);

        verify(recetaRepository).deleteByProducto_Id(5L);
        verify(recetaRepository, org.mockito.Mockito.times(2)).save(any(Receta.class));
        assertEquals(2, response.size());
        assertEquals("KG", response.get(0).getUnidadMedida());
        assertEquals("UND", response.get(1).getUnidadMedida());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoPathNoCoincideConBody() {
        RecetaRequest request = new RecetaRequest(5L, List.of(1L), List.of(new BigDecimal("1.00")), List.of("KG"));

        ApiException ex = assertThrows(ApiException.class, () -> recetaService.actualizar(6L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El id del path no coincide con productoId del body", ex.getMessage());
        verify(recetaRepository, never()).deleteByProducto_Id(any());
    }

    @Test
    void listarTodos_deberiaMapearRecetasOrdenadas() {
        Producto producto = crearProductoPreparado(1L, "Pizza", 1L);
        Insumo queso = crearInsumo(2L, "Queso");

        Receta receta = new Receta();
        receta.setId(100L);
        receta.setProducto(producto);
        receta.setInsumo(queso);
        receta.setCantidad(new BigDecimal("0.50"));
        receta.setUnidad_medida("KG");

        when(recetaRepository.findAllByOrderByProducto_IdAscIdAsc()).thenReturn(List.of(receta));

        List<RecetaResponse> response = recetaService.listarTodos();

        assertEquals(1, response.size());
        assertEquals(100L, response.get(0).getId());
        assertEquals("Pizza", response.get(0).getProductoNombre());
        assertEquals("Queso", response.get(0).getInsumoNombre());
    }

    private Producto crearProductoPreparado(Long id, String nombre, Long categoriaId) {
        Categoria categoria = new Categoria();
        categoria.setId(categoriaId);
        categoria.setNombre("CATEGORIA");

        Producto producto = new Producto();
        producto.setId(id);
        producto.setNombre(nombre);
        producto.setCategoria(categoria);
        return producto;
    }

    private Insumo crearInsumo(Long id, String nombre) {
        Insumo insumo = new Insumo();
        insumo.setId(id);
        insumo.setNombre(nombre);
        return insumo;
    }
}
