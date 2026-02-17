package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.CategoriaSimpleDTO;
import com.restobar.lapituca.dto.MarcaSimpleDTO;
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

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId()).orElseThrow(()-> new CategoriaNotFoundException("Categoria no encontrada"));

        Marca marca = marcaRepository.findById(request.getMarcaId()).orElseThrow(()-> new MarcaNotFoundException("Marca no encontrada"));

        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setCategoria(categoria);
        producto.setMarca(marca);

        Producto productoGuardado = productoRepository.save(producto);

        return new ProductoResponse(
                productoGuardado.getId(),
                productoGuardado.getNombre(),
                productoGuardado.getPrecio(),
                productoGuardado.getStock(),
                new CategoriaSimpleDTO(
                        categoria.getId(),
                        categoria.getNombre()
                ),
                new MarcaSimpleDTO(
                        marca.getId(),
                        marca.getNombre()
                )
        );
    }

    public List<Producto> listarTodos(){
        return productoRepository.findAll();
    }

    public Producto obtenerPorId(Long id){
        return productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));
    }

    public Producto actualizar(Long id, Producto producto){
        Producto productoExistente = productoRepository.findById(id).orElseThrow(() -> new ProductoNotFoundException("Producto no encontrado"));
        productoExistente.setNombre(producto.getNombre());
        productoExistente.setPrecio(producto.getPrecio());
        productoExistente.setStock(producto.getStock());
        productoExistente.setFecha_modificacion(LocalDate.now());

        return productoRepository.save(productoExistente);
    }

    public void eliminar(Long id){
        productoRepository.deleteById(id);
    }

}
