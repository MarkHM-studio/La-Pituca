package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ProductoRequest;
import com.restobar.lapituca.dto.response.CategoriaResponse;
import com.restobar.lapituca.dto.response.MarcaResponse;
import com.restobar.lapituca.dto.response.ProductoResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.CategoriaRepository;
import com.restobar.lapituca.repository.MarcaRepository;
import com.restobar.lapituca.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    @Transactional //Garantiza que las operaciones se ejecuten todas o ninguna. Si algo falla se hace RollBack(deshacer todo)
    public ProductoResponse guardar(ProductoRequest request){
        if (productoRepository.existsByNombre(request.getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un producto con ese nombre");
        }

        Categoria categoria = findCategoria(request.getCategoriaId());
        Producto producto = new Producto();
        aplicarDatos(producto, request, categoria);

        return mapToResponse(productoRepository.save(producto));
    }

    public List<ProductoResponse> listarTodos() {
        return productoRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    public ProductoResponse obtenerPorId(Long id) {
        return mapToResponse(findProducto(id));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request) {
        Producto producto = findProducto(id);

        if (productoRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un producto con ese nombre");
        }

        Categoria categoria = findCategoria(request.getCategoriaId());
        aplicarDatos(producto, request, categoria);

        return mapToResponse(productoRepository.save(producto));
    }

    public void eliminar(Long id) {
        if (!productoRepository.existsById(id)) {
            throw new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: " + id + " no encontrado");
        }
        productoRepository.deleteById(id);
    }

    private void aplicarDatos(Producto producto, ProductoRequest request, Categoria categoria) {
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setCategoria(categoria);

        if (isCategoriaPreparada(categoria.getId())) {
            if (request.getStock() != null && request.getStock() != 0) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Los productos de categoría 1 o 2 deben registrarse con stock 0");
            }
            producto.setStock(0);
        } else {
            if (request.getStock() == null) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El stock es obligatorio para productos de consumo directo");
            }
            producto.setStock(request.getStock());
        }

        Marca marca = null;
        if (request.getMarcaId() != null) {
            marca = marcaRepository.findById(request.getMarcaId())
                    .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Marca con id: " + request.getMarcaId() + " no encontrada"));
        }
        producto.setMarca(marca);
    }

    private boolean isCategoriaPreparada(Long categoriaId) {
        return categoriaId == 1L || categoriaId == 2L;
    }

    private Producto findProducto(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: " + id + " no encontrado"));
    }

    private Categoria findCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Categoria con id: " + id + " no encontrada"));
    }

    private ProductoResponse mapToResponse(Producto p) {
        CategoriaResponse categoriaResponse = new CategoriaResponse(
                p.getCategoria().getId(),
                p.getCategoria().getNombre(),
                p.getCategoria().getFechaHora_registro(),
                p.getCategoria().getFechaHora_actualizacion()
        );

        MarcaResponse marcaResponse = p.getMarca() == null ? null : new MarcaResponse(
                p.getMarca().getId(),
                p.getMarca().getNombre(),
                p.getMarca().getFechaHora_registro(),
                p.getMarca().getFechaHora_actualizacion()
        );

        return new ProductoResponse(p.getId(), p.getNombre(), p.getPrecio(), p.getStock(), categoriaResponse, marcaResponse);
    }

}
