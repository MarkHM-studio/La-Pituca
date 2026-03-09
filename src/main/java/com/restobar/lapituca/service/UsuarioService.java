package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.UsuarioRequest;
import com.restobar.lapituca.dto.UsuarioResponse;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioResponse guardar(UsuarioRequest request){

        if (usuarioRepository.existsByUsername(request.getUsername())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Usuario con este nombre");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword());
        usuario.setEstado("ACTIVO");
        usuarioRepository.save(usuario);

        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getEstado(),
                usuario.getTipo_usuario(),
                usuario.getFechaHora_registro(),
                usuario.getFechaHora_actualizacion()
        );
    }

    public List<UsuarioResponse> listarTodos(){
        return usuarioRepository.findAll().stream().map(usuario -> new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getTipo_usuario(),
                usuario.getEstado(),
                usuario.getFechaHora_registro(),
                usuario.getFechaHora_actualizacion()
        )).toList();
    }

    public UsuarioResponse obtenerPorId(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+id+" no encontrada"));
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getPassword(),
                usuario.getTipo_usuario(),
                usuario.getEstado(),
                usuario.getFechaHora_registro(),
                usuario.getFechaHora_actualizacion()
        );
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request){
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+id+" no encontrada"));

        if (usuarioRepository.existsByUsernameAndIdNot(request.getUsername(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Usuario con este nombre");
        }

        usuarioExistente.setUsername(request.getUsername());
        usuarioRepository.save(usuarioExistente);
        return new UsuarioResponse(
                usuarioExistente.getId(),
                usuarioExistente.getUsername(),
                usuarioExistente.getPassword(),
                usuarioExistente.getTipo_usuario(),
                usuarioExistente.getEstado(),
                usuarioExistente.getFechaHora_registro(),
                usuarioExistente.getFechaHora_actualizacion()
        );
    }

    public void eliminar(Long id){
        //Verificar que existe
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+id+" no encontrada"));
        usuarioRepository.delete(usuario);
    }
}
