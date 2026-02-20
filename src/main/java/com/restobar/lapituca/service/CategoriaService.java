package com.restobar.lapituca.service;

import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.exception.CategoriaNotFoundException;
import com.restobar.lapituca.repository.CategoriaRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public Categoria guardar(Categoria categoria){
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> listarTodos(){
        return categoriaRepository.findAll();
    }

    public Categoria obtenerPorId(Long id){
        return categoriaRepository.findById(id).orElseThrow(()
                -> new CategoriaNotFoundException("Categoria no encntrada"));
    }

    public Categoria actualizar(Long id, Categoria categoria){
        Categoria categoriaExistente = categoriaRepository.findById(id).orElseThrow(()
                -> new CategoriaNotFoundException("Categoria no encontrada"));

        categoriaExistente.setNombre(categoria.getNombre());

        return categoriaRepository.save(categoriaExistente);
    }

    public void eliminar(Long id){
        categoriaRepository.deleteById(id);
    }
}
