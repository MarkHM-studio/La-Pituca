package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.RolRequest;
import com.restobar.lapituca.dto.response.RolResponse;
import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.RolRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RolService {

    private final RolRepository rolRepository;

    public RolResponse guardar(RolRequest request){

        if (rolRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Rol con ese nombre");
        }

        Rol nuevoRol = new Rol();
        nuevoRol.setNombre(request.getNombre());
        rolRepository.save(nuevoRol);

        return new RolResponse(
                nuevoRol.getId(),
                nuevoRol.getNombre(),
                nuevoRol.getFechaHora_registro(),
                nuevoRol.getFechaHora_actualizacion()
        );
    }

    public List<RolResponse> listarTodos(){
        return rolRepository.findAll().stream().map(rol -> new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getFechaHora_registro(),
                rol.getFechaHora_actualizacion()
        )).toList();
    }

    public RolResponse obtenerPorId(Long id){
        Rol rol = rolRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Rol con id: "+id+" no encontrado"));
        return new RolResponse(
                rol.getId(),
                rol.getNombre(),
                rol.getFechaHora_registro(),
                rol.getFechaHora_actualizacion()
        );
    }

    public RolResponse actualizar(Long id, RolRequest request){

        Rol rolExistente = rolRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Rol con id: "+id+" no encontrado"));

        if (rolRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Rol con ese nombre");
        }

        rolExistente.setNombre(request.getNombre());

        Rol rolActualizado = rolRepository.save(rolExistente);

        return new RolResponse(
                rolActualizado.getId(),
                rolActualizado.getNombre(),
                rolActualizado.getFechaHora_registro(),
                rolActualizado.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){

        Rol rol = rolRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Rol con id:"+id+" no encontrado"));

        rolRepository.delete(rol);
    }
}
