package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.MesaRequest;
import com.restobar.lapituca.dto.MesaResponse;
import com.restobar.lapituca.entity.Mesa;
import com.restobar.lapituca.exception.MesaNotFoundException;
import com.restobar.lapituca.repository.MesaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaResponse guardar(MesaRequest request){
        Mesa mesa = new Mesa();
        mesa.setNombre(request.getNombre());
        mesa.setEstado("DESOCUPADO");
        mesaRepository.save(mesa);

        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public List<MesaResponse> listarTodos(){
        return mesaRepository.findAll()
                .stream()
                .map(mesa -> new MesaResponse(
                        mesa.getId(),
                        mesa.getNombre(),
                        mesa.getEstado(),
                        mesa.getFechaHora_registro(),
                        mesa.getFechaHora_actualizacion()
                ))
                .toList();//Modificar para mostrar solo las mesas con estado = DESOCUPADO
    }

    public MesaResponse obtenerPorId(Long id){
        Mesa mesa = mesaRepository.findById(id).orElseThrow(() ->new MesaNotFoundException("Mesa no encontrada"));
        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public MesaResponse actualizar(Long id, MesaRequest request){

        Mesa mesa = mesaRepository.findById(id).orElseThrow(() ->new MesaNotFoundException("Mesa no encontrada"));
        mesa.setNombre(request.getNombre());
        mesaRepository.save(mesa);

        return new MesaResponse(
                mesa.getId(),
                mesa.getNombre(),
                mesa.getEstado(),
                mesa.getFechaHora_registro(),
                mesa.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        mesaRepository.deleteById(id);
    }
}
