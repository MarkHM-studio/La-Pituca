package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.TipoEntregaRequest;
import com.restobar.lapituca.entity.TipoEntrega;
import com.restobar.lapituca.exception.TipoEntregaNotFoundException;
import com.restobar.lapituca.repository.TipoEntregaRepository;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoEntregaService {

    private final TipoEntregaRepository tipoEntregaRepository;

    public TipoEntrega guardar(TipoEntregaRequest request){
        TipoEntrega tipoEntrega = new TipoEntrega();
        tipoEntrega.setNombre(request.getNombre());
        return tipoEntregaRepository.save(tipoEntrega);
    }

    public List<TipoEntrega> listarTodos(){
        return tipoEntregaRepository.findAll();
    }

    public TipoEntrega obtenerPorId(Long id){
        return tipoEntregaRepository.findById(id).orElseThrow(()-> new TipoEntregaNotFoundException("Tipo de Entrega no encontrada"));
    }

    public TipoEntrega actualizar(Long id, TipoEntregaRequest request){
        TipoEntrega tipoEntregaExistente = tipoEntregaRepository.findById(id).orElseThrow(()-> new TipoEntregaNotFoundException("Tipo de Entrega no encontrada"));
        tipoEntregaExistente.setNombre(request.getNombre());
        return tipoEntregaRepository.save(tipoEntregaExistente);
    }

    public void eliminar(Long id){
        TipoEntrega tipoEntregaExistente = tipoEntregaRepository.findById(id).orElseThrow(()-> new TipoEntregaNotFoundException("Tipo de Entrega no encontrada"));
        tipoEntregaRepository.deleteById(id);
    }

}
