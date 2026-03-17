package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.RecetaRequest;
import com.restobar.lapituca.dto.response.RecetaResponse;
import com.restobar.lapituca.entity.Insumo;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.entity.Receta;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.InsumoRepository;
import com.restobar.lapituca.repository.ProductoRepository;
import com.restobar.lapituca.repository.RecetaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final ProductoRepository productoRepository;
    private final InsumoRepository insumoRepository;

    @Transactional
    public List<RecetaResponse> crear(RecetaRequest request) {
        Producto producto = validarProductoPreparado(request.getProductoId());
        validarEstructura(request);

        if (recetaRepository.existsByProducto_Id(producto.getId())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El producto ya tiene receta. Usa actualizar");
        }

        return guardarRecetas(producto, request);
    }

    @Transactional
    public List<RecetaResponse> actualizar(Long productoId, RecetaRequest request) {
        if (!productoId.equals(request.getProductoId())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El id del path no coincide con productoId del body");
        }
        Producto producto = validarProductoPreparado(productoId);
        validarEstructura(request);

        recetaRepository.deleteByProducto_Id(producto.getId());
        return guardarRecetas(producto, request);
    }

    public List<RecetaResponse> listarTodos() {
        return recetaRepository.findAllByOrderByProducto_IdAscIdAsc().stream().map(this::map).toList();
    }

    private List<RecetaResponse> guardarRecetas(Producto producto, RecetaRequest request) {
        List<RecetaResponse> out = new ArrayList<>();

        for (int i = 0; i < request.getInsumosId().size(); i++) {
            Long insumoId = request.getInsumosId().get(i);
            Insumo insumo = insumoRepository.findById(insumoId)
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Insumo con id: " + insumoId + " no encontrado"));

            Receta receta = new Receta();
            receta.setProducto(producto);
            receta.setInsumo(insumo);
            receta.setCantidad(request.getCantidades().get(i));
            receta.setUnidad_medida(normalize(request.getUnidadesMedida().get(i)));

            out.add(map(recetaRepository.save(receta)));
        }

        return out;
    }

    private Producto validarProductoPreparado(Long productoId) {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: " + productoId + " no encontrado"));

        if (producto.getCategoria() == null || (producto.getCategoria().getId() != 1L && producto.getCategoria().getId() != 2L)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Solo se aceptan recetas para productos de categoría 1 o 2");
        }
        return producto;
    }

    private void validarEstructura(RecetaRequest request) {
        if (request.getInsumosId().size() != request.getCantidades().size()
                || request.getInsumosId().size() != request.getUnidadesMedida().size()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "insumosId, cantidades y unidadesMedida deben tener igual tamaño");
        }
        if (request.getInsumosId().isEmpty()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Debe registrar al menos un insumo");
        }

        Set<Long> unique = new HashSet<>(request.getInsumosId());
        if (unique.size() != request.getInsumosId().size()) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "No se puede repetir el mismo insumo en una receta");
        }
    }

    private String normalize(String unit) {
        return unit.trim().toUpperCase();
    }

    private RecetaResponse map(Receta receta) {
        return new RecetaResponse(
                receta.getId(),
                receta.getProducto().getId(),
                receta.getProducto().getNombre(),
                receta.getInsumo().getId(),
                receta.getInsumo().getNombre(),
                receta.getCantidad(),
                receta.getUnidad_medida()
        );
    }
}
