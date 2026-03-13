package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.SucursalRequest;
import com.restobar.lapituca.dto.response.SucursalResponse;
import com.restobar.lapituca.entity.Sucursal;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.SucursalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SucursalService {
    private final SucursalRepository sucursalRepository;

    public SucursalResponse guardar(SucursalRequest request){

        if (sucursalRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Sucursal con ese nombre");
        }

        Sucursal sucursal = new Sucursal();
        sucursal.setNombre(request.getNombre());
        sucursal.setDireccion(request.getDireccion());
        sucursal.setRUC(request.getRUC());
        sucursalRepository.save(sucursal);

        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getRUC(),
                sucursal.getFechaHora_registro(),
                sucursal.getFechaHora_actualizacion()
        );
    }

    public List<SucursalResponse> listarTodos(){
        return sucursalRepository.findAll().stream().map(sucursal -> new SucursalResponse(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getRUC(),
                sucursal.getFechaHora_registro(),
                sucursal.getFechaHora_actualizacion()
        )).toList();
    }

    public SucursalResponse obtenerPorId(Long id){
        Sucursal sucursal = sucursalRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Sucursal con id: "+id+" no encontrada"));
        return new SucursalResponse(
                sucursal.getId(),
                sucursal.getNombre(),
                sucursal.getDireccion(),
                sucursal.getRUC(),
                sucursal.getFechaHora_registro(),
                sucursal.getFechaHora_actualizacion()
        );
    }

    public SucursalResponse actualizar(Long id, SucursalRequest request){

        Sucursal sucursalExistente = sucursalRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Sucursal con id: "+id+" no encontrada"));

        if (sucursalRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe una Sucursal con ese nombre");
        }

        sucursalExistente.setNombre(request.getNombre());
        sucursalExistente.setDireccion(request.getDireccion());
        sucursalExistente.setRUC(request.getRUC());

        Sucursal sucursalActualizada = sucursalRepository.save(sucursalExistente);

        return new SucursalResponse(
                sucursalActualizada.getId(),
                sucursalActualizada.getNombre(),
                sucursalActualizada.getDireccion(),
                sucursalActualizada.getRUC(),
                sucursalActualizada.getFechaHora_registro(),
                sucursalActualizada.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){

        Sucursal sucursal= sucursalRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Sucursal con id:"+id+" no encontrada"));

        sucursalRepository.delete(sucursal);
    }
}
