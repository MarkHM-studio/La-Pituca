package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.DistritoRequest;
import com.restobar.lapituca.dto.response.DistritoResponse;
import com.restobar.lapituca.entity.Distrito;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.DistritoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DistritoService {

    private final DistritoRepository distritoRepository;

    public DistritoResponse guardar(DistritoRequest request) {
        if (distritoRepository.existsByNombreIgnoreCase(request.getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un distrito con ese nombre");
        }

        Distrito distrito = new Distrito();
        distrito.setNombre(request.getNombre().trim());
        return toResponse(distritoRepository.save(distrito));
    }

    public List<DistritoResponse> listarTodos() {
        return distritoRepository.findAll().stream().map(this::toResponse).toList();
    }

    public DistritoResponse obtenerPorId(Long id) {
        Distrito distrito = distritoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Distrito con id: " + id + " no encontrado"));
        return toResponse(distrito);
    }

    public DistritoResponse actualizar(Long id, DistritoRequest request) {
        Distrito distrito = distritoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Distrito con id: " + id + " no encontrado"));

        if (distritoRepository.existsByNombreIgnoreCaseAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un distrito con ese nombre");
        }

        distrito.setNombre(request.getNombre().trim());
        return toResponse(distritoRepository.save(distrito));
    }

    public void eliminar(Long id) {
        Distrito distrito = distritoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Distrito con id: " + id + " no encontrado"));
        distritoRepository.delete(distrito);
    }

    private DistritoResponse toResponse(Distrito distrito) {
        return new DistritoResponse(
                distrito.getId(),
                distrito.getNombre(),
                distrito.getFechaHora_registro(),
                distrito.getFechaHora_actualizacion()
        );
    }
}