package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.HorarioRequest;
import com.restobar.lapituca.dto.response.HorarioResponse;
import com.restobar.lapituca.entity.Horario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.HorarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HorarioService {

    private final HorarioRepository horarioRepository;

    public HorarioResponse guardar(HorarioRequest request){

        if (request.getHora_inicio().equals(request.getHora_fin())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "La hora de inicio y fin no pueden ser iguales"
            );
        }

        if (horarioRepository.existsByHoraInicioAndHoraFin(
                request.getHora_inicio(),
                request.getHora_fin())) {

            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Ya existe un horario con esa hora de inicio y fin"
            );
        }
        /*
        if (horarioRepository.existsByHoraInicio(request.getHora_inicio())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Horario con esa hora de inicio");
        }
        if (horarioRepository.existsByHoraFin(request.getHora_fin())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Horario con esa hora de fin");
        }*/
        validarDuracion(request.getHora_inicio(), request.getHora_fin());

        Horario horario = new Horario();
        horario.setHoraInicio(request.getHora_inicio());
        horario.setHoraFin(request.getHora_fin());
        horarioRepository.save(horario);

        return new HorarioResponse(
                horario.getId(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getFechaHora_registro(),
                horario.getFechaHora_actualizacion()
        );
    }

    public List<HorarioResponse> listarTodos(){
        return horarioRepository.findAll().stream().map(horario -> new HorarioResponse(
                horario.getId(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getFechaHora_registro(),
                horario.getFechaHora_actualizacion()
        )).toList();
    }

    public HorarioResponse obtenerPorId(Long id){
        Horario horario = horarioRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Horario con id: "+id+" no encontrado"));
        return new HorarioResponse(
                horario.getId(),
                horario.getHoraInicio(),
                horario.getHoraFin(),
                horario.getFechaHora_registro(),
                horario.getFechaHora_actualizacion()
        );
    }

    public HorarioResponse actualizar(Long id, HorarioRequest request) {

        Horario horarioExistente = horarioRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Horario con id: " + id + " no encontrado"));

        if (request.getHora_inicio().equals(request.getHora_fin())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "La hora de inicio y fin no pueden ser iguales"
            );
        }

        if (horarioRepository.existsByHoraInicioAndHoraFinAndIdNot(
                request.getHora_inicio(),
                request.getHora_fin(),
                id)) {

            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Ya existe un horario con esa hora de inicio y fin"
            );
        }
        /*
        if (horarioRepository.existsByHoraInicioAndIdNot(request.getHora_inicio(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Horario con esa hora de inicio");
        }

        if (horarioRepository.existsByHoraFinAndIdNot(request.getHora_fin(), id)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Horario con esa hora de fin");
        }*/
        validarDuracion(request.getHora_inicio(), request.getHora_fin());

        horarioExistente.setHoraInicio(request.getHora_inicio());
        horarioExistente.setHoraFin(request.getHora_fin());

        Horario horarioActualizado = horarioRepository.save(horarioExistente);

        return new HorarioResponse(
                horarioActualizado.getId(),
                horarioActualizado.getHoraInicio(),
                horarioActualizado.getHoraFin(),
                horarioActualizado.getFechaHora_registro(),
                horarioActualizado.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        Horario horario = horarioRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Horario con id:"+id+" no encontrado"));

        horarioRepository.delete(horario);
    }

    private void validarDuracion(LocalTime inicio, LocalTime fin){

        long duracion;

        if (fin.isAfter(inicio)) {
            duracion = java.time.Duration.between(inicio, fin).toHours();
        } else {
            duracion = java.time.Duration.between(inicio, fin.plusHours(24)).toHours();
        }

        if (duracion > 9) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "El horario no puede durar más de 16 horas"
            );
        }
    }
}