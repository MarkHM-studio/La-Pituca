package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.InsumoRequest;
import com.restobar.lapituca.dto.response.InsumoResponse;
import com.restobar.lapituca.entity.Insumo;
import com.restobar.lapituca.entity.Marca;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.InsumoRepository;
import com.restobar.lapituca.repository.MarcaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsumoService {

    private final InsumoRepository insumoRepository;
    private final MarcaRepository marcaRepository;

    @Transactional
    public InsumoResponse crear(InsumoRequest request) {
        if (insumoRepository.existsByNombre(request.getNombre())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un insumo con ese nombre");
        }

        Insumo insumo = new Insumo();
        insumo.setNombre(request.getNombre());
        insumo.setPrecio(request.getPrecio());
        insumo.setStock(BigDecimal.ZERO);
        insumo.setUnidad_medida(normalizeUnit(request.getUnidadMedida()));
        insumo.setMarca(findMarca(request.getMarcaId()));

        return map(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoResponse actualizarRolAlmacenero(Long id, InsumoRequest request) {
        Insumo insumo = findInsumo(id);
        if (insumoRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un insumo con ese nombre");
        }

        insumo.setNombre(request.getNombre());
        insumo.setPrecio(request.getPrecio());
        insumo.setMarca(findMarca(request.getMarcaId()));

        return map(insumoRepository.save(insumo));
    }

    @Transactional
    public InsumoResponse actualizarRolAdmin(Long id, InsumoRequest request) {
        Insumo insumo = findInsumo(id);
        if (insumoRepository.existsByNombreAndIdNot(request.getNombre(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un insumo con ese nombre");
        }

        insumo.setNombre(request.getNombre());
        insumo.setPrecio(request.getPrecio());
        if (request.getStock() != null) {
            if (request.getStock().compareTo(BigDecimal.ZERO) < 0) {
                throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "El stock del insumo no puede ser negativo");
            }
            insumo.setStock(request.getStock());
        }
        insumo.setUnidad_medida(normalizeUnit(request.getUnidadMedida()));
        insumo.setMarca(findMarca(request.getMarcaId()));

        return map(insumoRepository.save(insumo));
    }

    public List<InsumoResponse> listarTodos() {
        return insumoRepository.findAll().stream().map(this::map).toList();
    }

    public InsumoResponse obtenerPorId(Long id){
        Insumo insumo = insumoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Insumo con id: "+id+" no encontrado"));
        return map(insumo);
    }

    private Insumo findInsumo(Long id) {
        return insumoRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Insumo con id: " + id + " no encontrado"));
    }

    private Marca findMarca(Long marcaId) {
        if (marcaId == null) {
            return null;
        }
        return marcaRepository.findById(marcaId)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Marca con id: " + marcaId + " no encontrada"));
    }

    private String normalizeUnit(String unit) {
        return unit == null ? null : unit.trim().toUpperCase();
    }

    private InsumoResponse map(Insumo insumo) {
        return new InsumoResponse(
                insumo.getId(),
                insumo.getNombre(),
                insumo.getPrecio(),
                insumo.getStock(),
                insumo.getUnidad_medida(),
                insumo.getMarca() != null ? insumo.getMarca().getId() : null
        );
    }
}
