package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TipoJornadaRequest;
import com.restobar.lapituca.dto.response.TipoJornadaResponse;
import com.restobar.lapituca.entity.TipoJornada;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoJornadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoJornadaService {

    private final TipoJornadaRepository tipoJornadaRepository;

    public TipoJornadaResponse guardar(TipoJornadaRequest request){

        if (tipoJornadaRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Jornada con ese nombre");
        }

        TipoJornada tipoJornada = new TipoJornada();
        tipoJornada.setNombre(request.getNombre());
        tipoJornadaRepository.save(tipoJornada);

        return new TipoJornadaResponse(
                tipoJornada.getId(),
                tipoJornada.getNombre(),
                tipoJornada.getFechaHora_registro(),
                tipoJornada.getFechaHora_actualizacion()
        );
    }

    public List<TipoJornadaResponse> listarTodos(){
        return tipoJornadaRepository.findAll().stream().map(tipoJornada -> new TipoJornadaResponse(
                tipoJornada.getId(),
                tipoJornada.getNombre(),
                tipoJornada.getFechaHora_registro(),
                tipoJornada.getFechaHora_actualizacion()
        )).toList();
    }

    public TipoJornadaResponse obtenerPorId(Long id){
        TipoJornada tipoJornada = tipoJornadaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de Jornada con id: "+id+" no encontrado"));
        return new TipoJornadaResponse(
                tipoJornada.getId(),
                tipoJornada.getNombre(),
                tipoJornada.getFechaHora_registro(),
                tipoJornada.getFechaHora_actualizacion()
        );
    }

    public TipoJornadaResponse actualizar(Long id, TipoJornadaRequest request){

        TipoJornada tipoJornadaExistente = tipoJornadaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de Jornada con id: "+id+" no encontrado"));

        if (tipoJornadaRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Tipo de Jornada con ese nombre");
        }

        tipoJornadaExistente.setNombre(request.getNombre());

        TipoJornada tipoJornadaActualizado = tipoJornadaRepository.save(tipoJornadaExistente);

        return new TipoJornadaResponse(
                tipoJornadaActualizado.getId(),
                tipoJornadaActualizado.getNombre(),
                tipoJornadaActualizado.getFechaHora_registro(),
                tipoJornadaActualizado.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){

        TipoJornada tipoJornada = tipoJornadaRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de Jornada con id:"+id+" no encontrado"));

        tipoJornadaRepository.delete(tipoJornada);
    }
}
