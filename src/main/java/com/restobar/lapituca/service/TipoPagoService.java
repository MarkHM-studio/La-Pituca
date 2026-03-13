package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoPagoRequest;
import com.restobar.lapituca.dto.response.TipoPagoResponse;
import com.restobar.lapituca.entity.TipoPago;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPagoService {

    private final TipoPagoRepository tipoPagoRepository;

    public TipoPagoResponse guardar(TipoPagoRequest request){
        if (tipoPagoRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Pago con este nombre");
        }
        TipoPago tipoPago = new TipoPago();
        tipoPago.setNombre(request.getNombre());
        tipoPagoRepository.save(tipoPago);

        return new TipoPagoResponse(
                tipoPago.getId(),
                tipoPago.getNombre(),
                tipoPago.getFechaHora_registro(),
                tipoPago.getFechaHora_actualizacion()
        );
    }

    public List<TipoPagoResponse> listarTodos(){
        return tipoPagoRepository.findAll().stream().map(tipoPago -> new TipoPagoResponse(
                tipoPago.getId(),
                tipoPago.getNombre(),
                tipoPago.getFechaHora_registro(),
                tipoPago.getFechaHora_actualizacion()
        )).toList();
    }

    public TipoPagoResponse obtenerPorId(Long id){
        TipoPago tipoPago = tipoPagoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Pago con id: "+id+" no encontrada"));
        return new TipoPagoResponse(
                tipoPago.getId(),
                tipoPago.getNombre(),
                tipoPago.getFechaHora_registro(),
                tipoPago.getFechaHora_actualizacion()
        );
    }

    public TipoPagoResponse actualizar(Long id, TipoPagoRequest request){
        TipoPago tipoPagoExistente = tipoPagoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Pago con id: "+id+" no encontrada"));

        if (tipoPagoRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Pago con este nombre");
        }

        tipoPagoExistente.setNombre(request.getNombre());
        tipoPagoRepository.save(tipoPagoExistente);
        return new TipoPagoResponse(
                tipoPagoExistente.getId(),
                tipoPagoExistente.getNombre(),
                tipoPagoExistente.getFechaHora_registro(),
                tipoPagoExistente.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        //Verificar que existe
        TipoPago tipoPago = tipoPagoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Tipo de Pago con id: "+id+" no encontrada"));
        tipoPagoRepository.delete(tipoPago);
    }
}
