package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.TipoEntregaRequest;
import com.restobar.lapituca.dto.TipoEntregaResponse;
import com.restobar.lapituca.entity.TipoEntrega;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoEntregaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class  TipoEntregaService {

    private final TipoEntregaRepository tipoEntregaRepository;

    public TipoEntregaResponse guardar(TipoEntregaRequest request){

        if (tipoEntregaRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Entrega con este nombre");
        }

        TipoEntrega tipoEntrega = new TipoEntrega();
        tipoEntrega.setNombre(request.getNombre());
        tipoEntregaRepository.save(tipoEntrega);
        return new TipoEntregaResponse(
                tipoEntrega.getId(),
                tipoEntrega.getNombre(),
                tipoEntrega.getFechaHora_registro(),
                tipoEntrega.getFechaHora_actualizacion()
        );
    }

    public List<TipoEntregaResponse> listarTodos(){
        return tipoEntregaRepository.findAll().stream().map(tipoEntrega -> new TipoEntregaResponse(
                tipoEntrega.getId(),
                tipoEntrega.getNombre(),
                tipoEntrega.getFechaHora_registro(),
                tipoEntrega.getFechaHora_actualizacion()
        )).toList();
    }

    public TipoEntregaResponse obtenerPorId(Long id){
        TipoEntrega tipoEntrega = tipoEntregaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Entrega con id: "+id+" no encontrada"));
        return new TipoEntregaResponse(
                tipoEntrega.getId(),
                tipoEntrega.getNombre(),
                tipoEntrega.getFechaHora_registro(),
                tipoEntrega.getFechaHora_actualizacion()
        );
    }

    public TipoEntregaResponse actualizar(Long id, TipoEntregaRequest request){
        TipoEntrega tipoEntregaExistente = tipoEntregaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Entrega con id: "+id+" no encontrada"));

        if (tipoEntregaRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Entrega con este nombre");
        }

        tipoEntregaExistente.setNombre(request.getNombre());
        tipoEntregaRepository.save(tipoEntregaExistente);
        return new TipoEntregaResponse(
                tipoEntregaExistente.getId(),
                tipoEntregaExistente.getNombre(),
                tipoEntregaExistente.getFechaHora_registro(),
                tipoEntregaExistente.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        //Verificar que existe
        TipoEntrega tipoEntrega = tipoEntregaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Entrega con id: "+id+" no encontrada"));
        tipoEntregaRepository.delete(tipoEntrega);
    }

}
