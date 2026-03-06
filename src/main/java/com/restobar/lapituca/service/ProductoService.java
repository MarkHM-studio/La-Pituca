package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.CategoriaResponse;
import com.restobar.lapituca.dto.MarcaResponse;
import com.restobar.lapituca.dto.ProductoRequest;
import com.restobar.lapituca.dto.ProductoResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.exception.*;
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
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una producto con ese nombre");
        }

        Categoria categoriaExistente = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Categoria con id: "+request.getCategoriaId()+" no encontrada"));

        Marca marcaExistente = marcaRepository.findById(request.getMarcaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Marca con id: "+request.getMarcaId()+" no encontrada"));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoriaExistente);
        producto.setMarca(marcaExistente);

        Producto productoGuardado = productoRepository.save(producto);

        return new ProductoResponse(
                productoGuardado.getId(),
                productoGuardado.getNombre(),
                productoGuardado.getPrecio(),
                productoGuardado.getStock(),
                new CategoriaResponse(
                        categoriaExistente.getId(),
                        categoriaExistente.getNombre(),
                        categoriaExistente.getFechaHora_registro(),
                        categoriaExistente.getFechaHora_actualizacion()
                ),
                new MarcaResponse(
                        marcaExistente.getId(),
                        marcaExistente.getNombre(),
                        marcaExistente.getFechaHora_registro(),
                        marcaExistente.getFechaHora_actualizacion()
                )
        );
    }

    public List<ProductoResponse> listarTodos(){
        return productoRepository.findAll().stream().map(producto -> new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                new CategoriaResponse(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre(),
                        producto.getCategoria().getFechaHora_registro(),
                        producto.getCategoria().getFechaHora_actualizacion()
                ),
                new MarcaResponse(
                        producto.getMarca().getId(),
                        producto.getMarca().getNombre(),
                        producto.getMarca().getFechaHora_registro(),
                        producto.getMarca().getFechaHora_actualizacion()
                ))
        ).toList();
    }

    public ProductoResponse obtenerPorId(Long id){
        Producto producto = productoRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Producto con id: "+id+" no encontrado"));
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                new CategoriaResponse(
                        producto.getCategoria().getId(),
                        producto.getCategoria().getNombre(),
                        producto.getCategoria().getFechaHora_registro(),
                        producto.getCategoria().getFechaHora_actualizacion()
                ),
                new MarcaResponse(
                        producto.getMarca().getId(),
                        producto.getMarca().getNombre(),
                        producto.getMarca().getFechaHora_registro(),
                        producto.getMarca().getFechaHora_actualizacion()
                )
        );
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request){

        Producto productoExistente = productoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Producto con id: "+id+" no encontrado"));

        if (marcaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Producto con ese nombre");
        }

        productoExistente.setNombre(request.getNombre());
        productoExistente.setPrecio(request.getPrecio());
        productoExistente.setStock(request.getStock());

        Categoria categoriaExistente = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Categoria con id: "+request.getCategoriaId()+" no encontrada"));

        productoExistente.setCategoria(categoriaExistente);

        Marca marcaExistente = marcaRepository.findById(request.getMarcaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Marca con id: "+request.getMarcaId()+" no encontrada"));

        productoExistente.setMarca(marcaExistente);

        Producto productoActualizado = productoRepository.save(productoExistente);

        return new ProductoResponse(
                productoActualizado.getId(),
                productoActualizado.getNombre(),
                productoActualizado.getPrecio(),
                productoActualizado.getStock(),
                new CategoriaResponse(
                        categoriaExistente.getId(),
                        categoriaExistente.getNombre(),
                        categoriaExistente.getFechaHora_registro(),
                        categoriaExistente.getFechaHora_actualizacion()
                ),
                new MarcaResponse(
                        marcaExistente.getId(),
                        marcaExistente.getNombre(),
                        marcaExistente.getFechaHora_registro(),
                        marcaExistente.getFechaHora_actualizacion()
                ));
    }

    public void eliminar(Long id){
        Producto producto = productoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Producto con id: "+id+" no encontrado"));
        productoRepository.delete(producto);
    }

}
