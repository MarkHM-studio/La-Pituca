package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.TipoPagoRequest;
import com.restobar.lapituca.dto.TipoPagoResponse;
import com.restobar.lapituca.entity.TipoPago;
import com.restobar.lapituca.exception.TipoPagoNotFoundException;
import com.restobar.lapituca.repository.TipoPagoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoPagoService {

    private final TipoPagoRepository tipoPagoRepository;

    public TipoPagoResponse guardar(TipoPagoRequest request){
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
        TipoPago tipoPago = tipoPagoRepository.findById(id).orElseThrow(()-> new TipoPagoNotFoundException("Tipo de Pago no encontrada"));
        return new TipoPagoResponse(
                tipoPago.getId(),
                tipoPago.getNombre(),
                tipoPago.getFechaHora_registro(),
                tipoPago.getFechaHora_actualizacion()
        );
    }

    public TipoPagoResponse actualizar(Long id, TipoPagoRequest request){
        TipoPago tipoPagoExistente = tipoPagoRepository.findById(id).orElseThrow(()-> new TipoPagoNotFoundException("Tipo de Pago no encontrada"));
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
        tipoPagoRepository.findById(id).orElseThrow(()-> new TipoPagoNotFoundException("Tipo de Pago no encontrada"));
        tipoPagoRepository.deleteById(id);
    }
}
