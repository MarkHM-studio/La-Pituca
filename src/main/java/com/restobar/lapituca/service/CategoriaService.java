package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.CategoriaRequest;
import com.restobar.lapituca.dto.CategoriaResponse;
import com.restobar.lapituca.entity.Categoria;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaResponse guardar(CategoriaRequest request){

        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Ya existe una Categoría con ese nombre"
            );
        }
        Categoria categoria = new Categoria();
        categoria.setNombre(request.getNombre());
        categoriaRepository.save(categoria);

        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getFechaHora_registro(),
                categoria.getFechaHora_actualizacion()
        );
    }

    public List<CategoriaResponse> listarTodos(){

        return categoriaRepository.findAll().stream().map(
                categoria -> (new CategoriaResponse(
                        categoria.getId(),
                        categoria.getNombre(),
                        categoria.getFechaHora_registro(),
                        categoria.getFechaHora_actualizacion()
                ))
        ).toList();
    }

    public CategoriaResponse obtenerPorId(Long id){
        Categoria categoria = categoriaRepository.findById(id).orElseThrow(()
                -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Categoria con id: " + id + " no encontrada"));
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getFechaHora_registro(),
                categoria.getFechaHora_actualizacion()
        );
    }

    public CategoriaResponse actualizar(Long id, CategoriaRequest request){

        Categoria categoriaExistente = categoriaRepository.findById(id).orElseThrow(()
                -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Categoria con id: " + id + " no encontrada"));

        if (categoriaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una categoría con ese nombre"
            );
        }

        categoriaExistente.setNombre(request.getNombre());
        Categoria categoriaActualizada =categoriaRepository.save(categoriaExistente);

        return new CategoriaResponse(
                categoriaActualizada.getId(),
                categoriaActualizada.getNombre(),
                categoriaActualizada.getFechaHora_registro(),
                categoriaActualizada.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Categoria con id: " + id + " no encontrada"
                ));

        categoriaRepository.delete(categoria);
    }
}
