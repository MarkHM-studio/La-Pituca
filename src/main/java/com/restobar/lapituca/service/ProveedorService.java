package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ProveedorRequest;
import com.restobar.lapituca.dto.response.ProveedorResponse;
import com.restobar.lapituca.entity.Proveedor;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional
    public ProveedorResponse guardar(ProveedorRequest request) {
        if (proveedorRepository.existsByCorreo(request.getCorreo())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un proveedor con ese correo");
        }
        if (proveedorRepository.existsByRUC(request.getRuc())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un proveedor con ese RUC");
        }

        Proveedor proveedor = new Proveedor();
        aplicarDatos(proveedor, request);

        return map(proveedorRepository.save(proveedor));
    }

    public List<ProveedorResponse> listarTodos() {
        return proveedorRepository.findAll().stream().map(this::map).toList();
    }

    public ProveedorResponse obtenerPorId(Long id) {
        return map(buscarProveedor(id));
    }

    @Transactional
    public ProveedorResponse actualizar(Long id, ProveedorRequest request) {
        Proveedor proveedor = buscarProveedor(id);

        if (proveedorRepository.existsByCorreoAndIdNot(request.getCorreo(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un proveedor con ese correo");
        }
        if (proveedorRepository.existsByRUCAndIdNot(request.getRuc(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un proveedor con ese RUC");
        }

        aplicarDatos(proveedor, request);
        return map(proveedorRepository.save(proveedor));
    }

    public void eliminar(Long id) {
        Proveedor proveedor = buscarProveedor(id);
        proveedorRepository.delete(proveedor);
    }

    private Proveedor buscarProveedor(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Proveedor con id: " + id + " no encontrado"));
    }

    private void aplicarDatos(Proveedor proveedor, ProveedorRequest request) {
        proveedor.setContacto(request.getContacto());
        proveedor.setRazon_social(request.getRazonSocial());
        proveedor.setRUC(request.getRuc());
        proveedor.setDireccion(request.getDireccion());
        proveedor.setTelefono(request.getTelefono());
        proveedor.setCorreo(request.getCorreo());
        proveedor.setEstado(request.getEstado());
    }

    private ProveedorResponse map(Proveedor p) {
        return new ProveedorResponse(
                p.getId(),
                p.getContacto(),
                p.getRazon_social(),
                p.getRUC(),
                p.getDireccion(),
                p.getTelefono(),
                p.getCorreo(),
                p.getEstado(),
                p.getFechaHora_registro(),
                p.getFechaHora_actualizacion()
        );
    }
}
