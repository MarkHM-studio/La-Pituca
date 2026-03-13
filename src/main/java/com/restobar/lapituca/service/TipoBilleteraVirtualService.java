package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoBilleteraVirtualRequest;
import com.restobar.lapituca.dto.response.TipoBilleteraVirtualResponse;
import com.restobar.lapituca.entity.TipoBilleteraVirtual;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoBilleteraVirtualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoBilleteraVirtualService {

    private final TipoBilleteraVirtualRepository tipoBilleteraVirtualRepository;

    public TipoBilleteraVirtualResponse guardar(TipoBilleteraVirtualRequest request){

        if (tipoBilleteraVirtualRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Billetera Virtual con este nombre");
        }

        TipoBilleteraVirtual tipoBilleteraVirtual = new TipoBilleteraVirtual();

        tipoBilleteraVirtual.setNombre(request.getNombre());

        tipoBilleteraVirtualRepository.save(tipoBilleteraVirtual);

        return new TipoBilleteraVirtualResponse(
                tipoBilleteraVirtual.getId(),
                tipoBilleteraVirtual.getNombre(),
                tipoBilleteraVirtual.getFechaHora_registro(),
                tipoBilleteraVirtual.getFechaHora_actualizacion()
        );
    }

    public List<TipoBilleteraVirtualResponse> listarTodos(){

        return tipoBilleteraVirtualRepository.findAll().stream().map(
                tipoBilleteraVirtual -> new TipoBilleteraVirtualResponse(
                        tipoBilleteraVirtual.getId(),
                        tipoBilleteraVirtual.getNombre(),
                        tipoBilleteraVirtual.getFechaHora_registro(),
                        tipoBilleteraVirtual.getFechaHora_actualizacion()
                )).toList();
    }

    public TipoBilleteraVirtualResponse obtenerPorId(Long id){
        TipoBilleteraVirtual tipoBilleteraVirtual = tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Billetera Virtual con id: "+id+" no encontrado"));

        return new TipoBilleteraVirtualResponse(
                tipoBilleteraVirtual.getId(),
                tipoBilleteraVirtual.getNombre(),
                tipoBilleteraVirtual.getFechaHora_registro(),
                tipoBilleteraVirtual.getFechaHora_actualizacion()
        );
    }

    public TipoBilleteraVirtualResponse actualizar(Long id, TipoBilleteraVirtualRequest request){
        TipoBilleteraVirtual tipoBilleteraVirtualExistente = tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Billetera Virtual con id: "+id+" no encontrado"));

        if (tipoBilleteraVirtualRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Billetera Virtual con ese nombre");
        }

        tipoBilleteraVirtualExistente.setNombre(request.getNombre());

        tipoBilleteraVirtualRepository.save(tipoBilleteraVirtualExistente);

        return new TipoBilleteraVirtualResponse(
                tipoBilleteraVirtualExistente.getId(),
                tipoBilleteraVirtualExistente.getNombre(),
                tipoBilleteraVirtualExistente.getFechaHora_registro(),
                tipoBilleteraVirtualExistente.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        //Verificar que existe
        TipoBilleteraVirtual tipoBilleteraVirtual = tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Billetera Virtual con id: "+id+" no encontrado"));

        tipoBilleteraVirtualRepository.delete(tipoBilleteraVirtual);
    }
}
