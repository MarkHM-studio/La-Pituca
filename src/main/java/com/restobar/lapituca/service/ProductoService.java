package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.CategoriaResponse;
import com.restobar.lapituca.dto.MarcaResponse;
import com.restobar.lapituca.dto.ProductoRequest;
import com.restobar.lapituca.dto.ProductoResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.entity.Producto;
import com.restobar.lapituca.exception.CategoriaNotFoundException;
import com.restobar.lapituca.exception.MarcaNotFoundException;
import com.restobar.lapituca.exception.ProductoNotFoundException;
import com.restobar.lapituca.repository.CategoriaRepository;
import com.restobar.lapituca.repository.MarcaRepository;
import com.restobar.lapituca.repository.ProductoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final MarcaRepository marcaRepository;

    @Transactional //Garantiza que las operaciones se ejecuten todas o ninguna. Si algo falla se hace RollBack(deshacer todo)
    public ProductoResponse guardar(ProductoRequest request){

        Categoria categoriaExistente = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new CategoriaNotFoundException("Categoria no encontrada"));

        Marca marcaExistente = marcaRepository.findById(request.getMarcaId()).orElseThrow(()-> new MarcaNotFoundException("Marca no encontrada"));

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
                        categoriaExistente.getNombre()
                ),
                new MarcaResponse(
                        marcaExistente.getId(),
                        marcaExistente.getNombre()
                )
        );
    }

    public List<Producto> listarTodos(){
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id){
        return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));
    }

    @Transactional
    public ProductoResponse actualizar(Long id, ProductoRequest request){

        Producto productoExistente = productoRepository.findById(id).orElseThrow(()-> new ProductoNotFoundException("Producto no encontrado"));

        productoExistente.setNombre(request.getNombre());
        productoExistente.setPrecio(request.getPrecio());
        productoExistente.setStock(request.getStock());

        Categoria categoriaExistente = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new CategoriaNotFoundException("Categoria no encontrada"));

        productoExistente.setCategoria(categoriaExistente);

        Marca marcaExistente = marcaRepository.findById(request.getMarcaId()).orElseThrow(()-> new MarcaNotFoundException("Marca no encontrada"));

        productoExistente.setMarca(marcaExistente);

        Producto productoActualizado = productoRepository.save(productoExistente);

        return new ProductoResponse(
                productoActualizado.getId(),
                productoActualizado.getNombre(),
                productoActualizado.getPrecio(),
                productoActualizado.getStock(),
                new CategoriaResponse(
                        categoriaExistente.getId(),
                        categoriaExistente.getNombre()
                ),
                new MarcaResponse(
                        marcaExistente.getId(),
                        marcaExistente.getNombre()
                ));
    }

    public void eliminar(Long id){
        productoRepository.deleteById(id);
    }

}
