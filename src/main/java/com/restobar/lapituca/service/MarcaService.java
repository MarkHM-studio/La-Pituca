package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.MarcaRequest;
import com.restobar.lapituca.dto.MarcaResponse;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MarcaService {

    private final MarcaRepository marcaRepository;

    public MarcaResponse guardar(MarcaRequest request){

        if (marcaRepository.existsByNombre(request.getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Marca con ese nombre");
        }

        Marca marca = new Marca();
        marca.setNombre(request.getNombre());

        marcaRepository.save(marca);

        return new MarcaResponse(
                marca.getId(),
                marca.getNombre(),
                marca.getFechaHora_registro(),
                marca.getFechaHora_actualizacion()
        );
    }

    public List<MarcaResponse> listarTodos(){

        return marcaRepository.findAll().stream().map(
                marca -> new MarcaResponse(
                        marca.getId(),
                        marca.getNombre(),
                        marca.getFechaHora_registro(),
                        marca.getFechaHora_actualizacion()
                )
        ).toList();
    }

    public MarcaResponse obtenerPorId(Long id){

        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Marca con id: " + id + " no encontrada"));

        return new MarcaResponse(
                marca.getId(),
                marca.getNombre(),
                marca.getFechaHora_registro(),
                marca.getFechaHora_actualizacion()
        );
    }

    public MarcaResponse actualizar(Long id, MarcaRequest request){

        Marca marcaExistente = marcaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Marca con id: " + id + " no encontrada"));

        if (marcaRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Marca con ese nombre");
        }

        marcaExistente.setNombre(request.getNombre());

        Marca marcaActualizada = marcaRepository.save(marcaExistente);

        return new MarcaResponse(
                marcaActualizada.getId(),
                marcaActualizada.getNombre(),
                marcaActualizada.getFechaHora_registro(),
                marcaActualizada.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){

        Marca marca = marcaRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Marca con id: " + id + " no encontrada"));

        marcaRepository.delete(marca);
    }
}
