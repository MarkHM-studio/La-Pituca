package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.UsuarioRequest;
import com.restobar.lapituca.dto.response.UsuarioResponse;
import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.RolRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioResponse guardar(UsuarioRequest request){

        Rol rol = rolRepository.findById(request.getRolId()).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Rol con id: " + request.getRolId() + " no encontrado"));

        if (usuarioRepository.existsByUsername(request.getUsername())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Usuario con este nombre");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setEstado("ACTIVO");
        usuario.setProvider("LOCAL");
        usuario.setProveedorId(1);
        usuario.setRol(rol);
        usuario.setTipo_usuario(1);
        usuarioRepository.save(usuario);

        return toResponse(usuario);
    }

    public List<UsuarioResponse> listarTodos(){
        return usuarioRepository.findAll().stream().map(this::toResponse).toList();
    }

    public UsuarioResponse obtenerPorId(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: " + id + " no encontrada"));
        return toResponse(usuario);
    }

    public UsuarioResponse actualizar(Long id, UsuarioRequest request){
        Usuario usuarioExistente = usuarioRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: " + id + " no encontrada"));

        if (usuarioRepository.existsByUsernameAndIdNot(request.getUsername(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Usuario con este nombre");
        }

        Rol rol = rolRepository.findById(request.getRolId()).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Rol con id: " + request.getRolId() + " no encontrado"));

        usuarioExistente.setUsername(request.getUsername());
        usuarioExistente.setPassword(passwordEncoder.encode(request.getPassword()));
        usuarioExistente.setRol(rol);
        if (usuarioExistente.getProveedorId() == null) {
            usuarioExistente.setProveedorId(1);
        }
        if (usuarioExistente.getTipo_usuario() == null) {
            usuarioExistente.setTipo_usuario(1);
        }
        usuarioRepository.save(usuarioExistente);
        return toResponse(usuarioExistente);
    }

    public void eliminar(Long id){
        Usuario usuario = usuarioRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: " + id + " no encontrada"));
        usuarioRepository.delete(usuario);
    }

    private UsuarioResponse toResponse(Usuario usuario) {
        return new UsuarioResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getTipo_usuario(),
                usuario.getEstado(),
                usuario.getFechaHora_registro(),
                usuario.getFechaHora_actualizacion(),
                usuario.getRol().getId(),
                usuario.getRol().getNombre()
        );
    }
}