package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.InsumoRequest;
import com.restobar.lapituca.dto.response.InsumoResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Insumo;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.CategoriaRepository;
import com.restobar.lapituca.repository.InsumoRepository;
import com.restobar.lapituca.repository.MarcaRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InsumoServiceTest {

    @Mock
    private InsumoRepository insumoRepository;

    @Mock
    private MarcaRepository marcaRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @InjectMocks
    private InsumoService insumoService;

    @Test
    void crear_deberiaGuardarConStockCeroYUnidadNormalizada_cuandoRequestValido() {
        Categoria categoria = crearCategoria(3L);
        Marca marca = crearMarca(2L);
        InsumoRequest request = new InsumoRequest("Harina", new BigDecimal("6.50"), new BigDecimal("20.00"), " kg ", 2L, 3L);

        when(insumoRepository.existsByNombre("Harina")).thenReturn(false);
        when(marcaRepository.findById(2L)).thenReturn(Optional.of(marca));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(insumoRepository.save(any(Insumo.class))).thenAnswer(invocation -> {
            Insumo i = invocation.getArgument(0);
            i.setId(10L);
            return i;
        });

        InsumoResponse response = insumoService.crear(request);

        assertEquals(10L, response.getId());
        assertEquals("Harina", response.getNombre());
        assertEquals(new BigDecimal("0"), response.getStock());
        assertEquals("KG", response.getUnidadMedida());
        assertEquals(2L, response.getMarcaId());
        assertEquals(3L, response.getCategoriaId());
    }

    @Test
    void crear_deberiaPermitirMarcaNula() {
        Categoria categoria = crearCategoria(3L);
        InsumoRequest request = new InsumoRequest("Azucar", new BigDecimal("4.50"), null, "kg", null, 3L);

        when(insumoRepository.existsByNombre("Azucar")).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(categoria));
        when(insumoRepository.save(any(Insumo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        InsumoResponse response = insumoService.crear(request);

        assertNull(response.getMarcaId());
        assertEquals(3L, response.getCategoriaId());
    }

    @Test
    void crear_deberiaLanzarErrorNegocio_cuandoNombreDuplicado() {
        InsumoRequest request = new InsumoRequest("Harina", new BigDecimal("6.50"), null, "kg", null, 3L);
        when(insumoRepository.existsByNombre("Harina")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.crear(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un insumo con ese nombre", ex.getMessage());
        verify(insumoRepository, never()).save(any());
    }

    @Test
    void crear_deberiaLanzarError_cuandoCategoriaNoExiste() {
        InsumoRequest request = new InsumoRequest("Harina", new BigDecimal("6.50"), null, "kg", null, 30L);
        when(insumoRepository.existsByNombre("Harina")).thenReturn(false);
        when(categoriaRepository.findById(30L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.crear(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Categoría con id: 30 no encontrada", ex.getMessage());
    }

    @Test
    void crear_deberiaLanzarError_cuandoMarcaNoExiste() {
        InsumoRequest request = new InsumoRequest("Harina", new BigDecimal("6.50"), null, "kg", 88L, 3L);
        when(insumoRepository.existsByNombre("Harina")).thenReturn(false);
        when(marcaRepository.findById(88L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.crear(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Marca con id: 88 no encontrada", ex.getMessage());
    }

    @Test
    void actualizarRolAlmacenero_deberiaActualizarSinCambiarStockNiUnidad() {
        Insumo existente = crearInsumo(4L, "Sal", new BigDecimal("3.00"), new BigDecimal("100.00"), "KG");
        InsumoRequest request = new InsumoRequest("Sal Fina", new BigDecimal("3.50"), new BigDecimal("999.00"), "g", null, 3L);

        when(insumoRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(insumoRepository.existsByNombreAndIdNot("Sal Fina", 4L)).thenReturn(false);
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(crearCategoria(3L)));
        when(insumoRepository.save(existente)).thenReturn(existente);

        InsumoResponse response = insumoService.actualizarRolAlmacenero(4L, request);

        assertEquals("Sal Fina", response.getNombre());
        assertEquals(new BigDecimal("100.00"), response.getStock());
        assertEquals("KG", response.getUnidadMedida());
    }

    @Test
    void actualizarRolAlmacenero_deberiaLanzarError_cuandoNombreDuplicado() {
        Insumo existente = crearInsumo(4L, "Sal", new BigDecimal("3.00"), new BigDecimal("100.00"), "KG");
        InsumoRequest request = new InsumoRequest("Duplicado", new BigDecimal("3.50"), null, "g", null, 3L);

        when(insumoRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(insumoRepository.existsByNombreAndIdNot("Duplicado", 4L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.actualizarRolAlmacenero(4L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        verify(insumoRepository, never()).save(any());
    }

    @Test
    void actualizarRolAdmin_deberiaActualizarConStockYUnidad_cuandoValido() {
        Insumo existente = crearInsumo(4L, "Sal", new BigDecimal("3.00"), new BigDecimal("100.00"), "KG");
        InsumoRequest request = new InsumoRequest("Sal Fina", new BigDecimal("3.50"), new BigDecimal("50.00"), " g ", 2L, 3L);

        when(insumoRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(insumoRepository.existsByNombreAndIdNot("Sal Fina", 4L)).thenReturn(false);
        when(marcaRepository.findById(2L)).thenReturn(Optional.of(crearMarca(2L)));
        when(categoriaRepository.findById(3L)).thenReturn(Optional.of(crearCategoria(3L)));
        when(insumoRepository.save(existente)).thenReturn(existente);

        InsumoResponse response = insumoService.actualizarRolAdmin(4L, request);

        assertEquals("Sal Fina", response.getNombre());
        assertEquals(new BigDecimal("50.00"), response.getStock());
        assertEquals("G", response.getUnidadMedida());
        assertEquals(2L, response.getMarcaId());
    }

    @Test
    void actualizarRolAdmin_deberiaLanzarError_cuandoStockNegativo() {
        Insumo existente = crearInsumo(4L, "Sal", new BigDecimal("3.00"), new BigDecimal("100.00"), "KG");
        InsumoRequest request = new InsumoRequest("Sal Fina", new BigDecimal("3.50"), new BigDecimal("-1.00"), "g", null, 3L);

        when(insumoRepository.findById(4L)).thenReturn(Optional.of(existente));
        when(insumoRepository.existsByNombreAndIdNot("Sal Fina", 4L)).thenReturn(false);

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.actualizarRolAdmin(4L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("El stock del insumo no puede ser negativo", ex.getMessage());
        verify(insumoRepository, never()).save(any());
    }

    @Test
    void actualizarRolAdmin_deberiaLanzarNotFound_cuandoInsumoNoExiste() {
        InsumoRequest request = new InsumoRequest("Sal Fina", new BigDecimal("3.50"), new BigDecimal("1.00"), "g", null, 3L);
        when(insumoRepository.findById(404L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.actualizarRolAdmin(404L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Insumo con id: 404 no encontrado", ex.getMessage());
    }

    @Test
    void listarTodos_deberiaMapearResultados() {
        Insumo i1 = crearInsumo(1L, "Harina", new BigDecimal("6.00"), new BigDecimal("10.00"), "KG");
        i1.setCategoria(crearCategoria(3L));
        Insumo i2 = crearInsumo(2L, "Azucar", new BigDecimal("5.00"), new BigDecimal("8.00"), "KG");
        i2.setCategoria(crearCategoria(4L));

        when(insumoRepository.findAll()).thenReturn(List.of(i1, i2));

        List<InsumoResponse> response = insumoService.listarTodos();

        assertEquals(2, response.size());
        assertEquals("Harina", response.get(0).getNombre());
        assertEquals(4L, response.get(1).getCategoriaId());
    }

    @Test
    void obtenerPorId_deberiaRetornarInsumo_cuandoExiste() {
        Insumo insumo = crearInsumo(20L, "Pimienta", new BigDecimal("2.50"), new BigDecimal("3.00"), "G");
        insumo.setCategoria(crearCategoria(3L));
        when(insumoRepository.findById(20L)).thenReturn(Optional.of(insumo));

        InsumoResponse response = insumoService.obtenerPorId(20L);

        assertEquals(20L, response.getId());
        assertEquals("Pimienta", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaLanzarNotFound_cuandoNoExiste() {
        when(insumoRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> insumoService.obtenerPorId(999L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertEquals("Insumo con id: 999 no encontrado", ex.getMessage());
    }

    private Categoria crearCategoria(Long id) {
        Categoria categoria = new Categoria();
        categoria.setId(id);
        categoria.setNombre("CAT");
        return categoria;
    }

    private Marca crearMarca(Long id) {
        Marca marca = new Marca();
        marca.setId(id);
        marca.setNombre("MARCA");
        return marca;
    }

    private Insumo crearInsumo(Long id, String nombre, BigDecimal precio, BigDecimal stock, String unidad) {
        Insumo insumo = new Insumo();
        insumo.setId(id);
        insumo.setNombre(nombre);
        insumo.setPrecio(precio);
        insumo.setStock(stock);
        insumo.setUnidad_medida(unidad);
        return insumo;
    }
}