package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TurnoRequest;
import com.restobar.lapituca.dto.response.HorarioResponse;
import com.restobar.lapituca.dto.response.TurnoResponse;
import com.restobar.lapituca.entity.Horario;
import com.restobar.lapituca.entity.Turno;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.HorarioRepository;
import com.restobar.lapituca.repository.TurnoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final HorarioRepository horarioRepository;

    public TurnoResponse guardar(TurnoRequest request){

        if (turnoRepository.existsByNombre(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Turno con ese nombre");
        }

        Horario horario = horarioRepository.findById(request.getHorarioId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Horario con id: "+request.getHorarioId()+" no encontrado"));

        Turno turno = new Turno();
        turno.setNombre(request.getNombre());
        turno.setHorario(horario);
        turnoRepository.save(turno);

        return new TurnoResponse(
                turno.getId(),
                turno.getNombre(),
                turno.getFechaHora_registro(),
                turno.getFechaHora_actualizacion(),
                new HorarioResponse(
                        turno.getHorario().getId(),
                        turno.getHorario().getHoraInicio(),
                        turno.getHorario().getHoraFin(),
                        turno.getFechaHora_registro(),
                        turno.getFechaHora_actualizacion()
                )
        );
    }

    public List<TurnoResponse> listarTodos(){
        return turnoRepository.findAll().stream().map(turno -> new TurnoResponse(
                turno.getId(),
                turno.getNombre(),
                turno.getFechaHora_registro(),
                turno.getFechaHora_actualizacion(),
                new HorarioResponse(
                        turno.getHorario().getId(),
                        turno.getHorario().getHoraInicio(),
                        turno.getHorario().getHoraFin(),
                        turno.getFechaHora_registro(),
                        turno.getFechaHora_actualizacion()
                )
        )).toList();
    }

    public TurnoResponse obtenerPorId(Long id){
        Turno turno = turnoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Turno con id: "+id+" no encontrado"));
        return new TurnoResponse(
                turno.getId(),
                turno.getNombre(),
                turno.getFechaHora_registro(),
                turno.getFechaHora_actualizacion(),
                new HorarioResponse(
                        turno.getHorario().getId(),
                        turno.getHorario().getHoraInicio(),
                        turno.getHorario().getHoraFin(),
                        turno.getFechaHora_registro(),
                        turno.getFechaHora_actualizacion()
                )
        );
    }

    public TurnoResponse actualizar(Long id, TurnoRequest request){

        Turno turnoExistente = turnoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Turno con id: "+id+" no encontrado"));

        if (turnoRepository.existsByNombreAndIdNot(request.getNombre(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Turno con ese nombre");
        }

        Horario horario = horarioRepository.findById(request.getHorarioId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Horario con id: "+request.getHorarioId()+" no encontrado"));

        turnoExistente.setNombre(request.getNombre());
        turnoExistente.setHorario(horario);
        Turno turnoActualizado = turnoRepository.save(turnoExistente);

        return new TurnoResponse(
                turnoActualizado.getId(),
                turnoActualizado.getNombre(),
                turnoActualizado.getFechaHora_registro(),
                turnoActualizado.getFechaHora_actualizacion(),
                new HorarioResponse(
                        turnoActualizado.getHorario().getId(),
                        turnoActualizado.getHorario().getHoraInicio(),
                        turnoActualizado.getHorario().getHoraFin(),
                        turnoActualizado.getFechaHora_registro(),
                        turnoActualizado.getFechaHora_actualizacion()
                )
        );
    }

    public void eliminar(Long id){

        Turno turno = turnoRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Turno con id:"+id+" no encontrado"));

        turnoRepository.delete(turno);
    }
}
