package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TrabajadorRequest;
import com.restobar.lapituca.dto.response.TrabajadorResponse;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TrabajadorService {

    private final TrabajadorRepository trabajadorRepository;
    private final UsuarioRepository usuarioRepository;
    private final TurnoRepository turnoRepository;
    private final TipoJornadaRepository tipoJornadaRepository;

    public TrabajadorResponse guardar(TrabajadorRequest request){

        if (trabajadorRepository.existsByDni(request.getNombre())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Trabajador con ese DNI");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(()->new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        Turno turno = turnoRepository.findById(request.getTurnoId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Turno con id: "+request.getTurnoId()+" no encontrado"));

        TipoJornada tipoJornada = tipoJornadaRepository.findById(request.getTipoJornadaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de Jornada con id: "+request.getTipoJornadaId()+" no encontrado"));

        usuario.setTipo_usuario(2);
        usuarioRepository.save(usuario);

        Trabajador trabajador = new Trabajador();
        trabajador.setNombre(request.getNombre());
        trabajador.setApellido(request.getApellido());
        trabajador.setDni(request.getDni());
        trabajador.setTelefono(request.getTelefono());
        trabajador.setCorreo(request.getCorreo());
        trabajador.setFecha_inicio(request.getFechaInicio());
        trabajador.setFecha_fin(request.getFechaFin());
        trabajador.setEstado("ACTIVO");

        trabajador.setUsuario(usuario);
        trabajador.setTurno(turno);
        trabajador.setTipoJornada(tipoJornada);

        Trabajador trabajadorGuardado = trabajadorRepository.save(trabajador);

        return new TrabajadorResponse(
                trabajadorGuardado.getId(),
                trabajadorGuardado.getNombre(),
                trabajadorGuardado.getApellido(),
                trabajadorGuardado.getDni(),
                trabajadorGuardado.getTelefono(),
                trabajadorGuardado.getCorreo(),
                trabajadorGuardado.getFecha_inicio(),
                trabajadorGuardado.getFecha_fin(),
                trabajadorGuardado.getEstado(),
                trabajadorGuardado.getFechaHora_registro(),
                trabajadorGuardado.getFechaHora_actualizacion(),

                trabajadorGuardado.getUsuario().getId(),
                trabajadorGuardado.getUsuario().getUsername(),

                trabajadorGuardado.getTurno().getId(),
                trabajadorGuardado.getTurno().getNombre(),

                trabajadorGuardado.getTipoJornada().getId(),
                trabajadorGuardado.getTipoJornada().getNombre()
        );
    }

    public List<TrabajadorResponse> listarTodos(){
        return trabajadorRepository.findAll().stream().map(trabajador->new TrabajadorResponse(
                trabajador.getId(),
                trabajador.getNombre(),
                trabajador.getApellido(),
                trabajador.getDni(),
                trabajador.getTelefono(),
                trabajador.getCorreo(),
                trabajador.getFecha_inicio(),
                trabajador.getFecha_fin(),
                trabajador.getEstado(),
                trabajador.getFechaHora_registro(),
                trabajador.getFechaHora_actualizacion(),

                trabajador.getUsuario().getId(),
                trabajador.getUsuario().getUsername(),

                trabajador.getTurno().getId(),
                trabajador.getTurno().getNombre(),

                trabajador.getTipoJornada().getId(),
                trabajador.getTipoJornada().getNombre()
        )).toList();
    }

    public TrabajadorResponse obtenerPorId(Long id){
        Trabajador trabajador = trabajadorRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Trabajador con id: "+id+" no encontrado"));
        return new TrabajadorResponse(
                trabajador.getId(),
                trabajador.getNombre(),
                trabajador.getApellido(),
                trabajador.getDni(),
                trabajador.getTelefono(),
                trabajador.getCorreo(),
                trabajador.getFecha_inicio(),
                trabajador.getFecha_fin(),
                trabajador.getEstado(),
                trabajador.getFechaHora_registro(),
                trabajador.getFechaHora_actualizacion(),

                trabajador.getUsuario().getId(),
                trabajador.getUsuario().getUsername(),

                trabajador.getTurno().getId(),
                trabajador.getTurno().getNombre(),

                trabajador.getTipoJornada().getId(),
                trabajador.getTipoJornada().getNombre()
        );
    }

    public TrabajadorResponse actualizar(Long id, TrabajadorRequest request){

        Trabajador trabajadorExistente = trabajadorRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Trabajador con id: "+id+" no encontrado"));

        if (trabajadorRepository.existsByDniAndIdNot(request.getDni(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Trabajador con ese DNI");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(()->new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+request.getUsuarioId()+" no encontrado"));

        Turno turno = turnoRepository.findById(request.getTurnoId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Turno con id: "+request.getTurnoId()+" no encontrado"));

        TipoJornada tipoJornada = tipoJornadaRepository.findById(request.getTipoJornadaId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Tipo de Jornada con id: "+request.getTipoJornadaId()+" no encontrado"));

        trabajadorExistente.setNombre(request.getNombre());
        trabajadorExistente.setApellido(request.getApellido());
        trabajadorExistente.setDni(request.getDni());
        trabajadorExistente.setTelefono(request.getTelefono());
        trabajadorExistente.setCorreo(request.getCorreo());
        trabajadorExistente.setFecha_inicio(request.getFechaInicio());
        trabajadorExistente.setFecha_fin(request.getFechaFin());
        trabajadorExistente.setEstado("ACTIVO");

        trabajadorExistente.setUsuario(usuario);
        trabajadorExistente.setTurno(turno);
        trabajadorExistente.setTipoJornada(tipoJornada);

        Trabajador trabajadorActualizado = trabajadorRepository.save(trabajadorExistente);

        return new TrabajadorResponse(
                trabajadorActualizado.getId(),
                trabajadorActualizado.getNombre(),
                trabajadorActualizado.getApellido(),
                trabajadorActualizado.getDni(),
                trabajadorActualizado.getTelefono(),
                trabajadorActualizado.getCorreo(),
                trabajadorActualizado.getFecha_inicio(),
                trabajadorActualizado.getFecha_fin(),
                trabajadorActualizado.getEstado(),
                trabajadorActualizado.getFechaHora_registro(),
                trabajadorActualizado.getFechaHora_actualizacion(),

                trabajadorActualizado.getUsuario().getId(),
                trabajadorActualizado.getUsuario().getUsername(),

                trabajadorActualizado.getTurno().getId(),
                trabajadorActualizado.getTurno().getNombre(),

                trabajadorActualizado.getTipoJornada().getId(),
                trabajadorActualizado.getTipoJornada().getNombre()
        );
    }

    public void eliminar(Long id){
        Trabajador trabajador = trabajadorRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Trabajador con id:"+id+" no encontrado"));

        trabajadorRepository.delete(trabajador);
    }
}
