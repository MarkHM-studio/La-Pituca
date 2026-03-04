package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.TipoBilleteraVirtualRequest;
import com.restobar.lapituca.dto.TipoBilleteraVirtualResponse;
import com.restobar.lapituca.dto.TipoPagoRequest;
import com.restobar.lapituca.entity.TipoBilleteraVirtual;
import com.restobar.lapituca.exception.TipoBilleteraVirtualNotFoundException;
import com.restobar.lapituca.repository.TipoBilleteraVirtualRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoBilleteraVirtualService {

    private final TipoBilleteraVirtualRepository tipoBilleteraVirtualRepository;

    public TipoBilleteraVirtualResponse guardar(TipoBilleteraVirtualRequest request){
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
        TipoBilleteraVirtual tipoBilleteraVirtual = tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new TipoBilleteraVirtualNotFoundException("Tipo de Billetera Virtual no encontrado"));

        return new TipoBilleteraVirtualResponse(
                tipoBilleteraVirtual.getId(),
                tipoBilleteraVirtual.getNombre(),
                tipoBilleteraVirtual.getFechaHora_registro(),
                tipoBilleteraVirtual.getFechaHora_actualizacion()
        );
    }

    public TipoBilleteraVirtualResponse actualizar(Long id, TipoBilleteraVirtualRequest request){
        TipoBilleteraVirtual tipoBilleteraVirtualExistente = tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new TipoBilleteraVirtualNotFoundException("Tipo de Billetera Virtual no encontrado"));

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
        tipoBilleteraVirtualRepository.findById(id).orElseThrow(()-> new TipoBilleteraVirtualNotFoundException("Tipo de Billetera Virtual no encontrado"));

        tipoBilleteraVirtualRepository.deleteById(id);
    }
}
