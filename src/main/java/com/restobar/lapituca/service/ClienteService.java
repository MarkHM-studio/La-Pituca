package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ClienteRequest;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.entity.Cliente;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ClienteRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public ClienteResponse guardar(ClienteRequest request) {

        if (clienteRepository.existsByDni(request.getDni())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Cliente con ese DNI");
        }

        if (clienteRepository.existsByCorreo(request.getDni())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Cliente con ese Correo");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId()).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: "+request.getUsuarioId()+" no encontrado"));
        usuario.setTipo_usuario(1);
        usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setDni(request.getDni());
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreo(request.getCorreo());
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");/*NUEVO, AMATEUR, FRECUENTE, VIP*/
        cliente.setUsuario(usuario);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return new ClienteResponse(
                clienteGuardado.getId(),
                clienteGuardado.getNombre(),
                clienteGuardado.getApellido(),
                clienteGuardado.getDni(),
                clienteGuardado.getTelefono(),
                clienteGuardado.getCorreo(),
                clienteGuardado.getEstado(),
                clienteGuardado.getTipo_cliente(),
                clienteGuardado.getFechaHora_registro(),
                clienteGuardado.getFechaHora_actualizacion(),

                clienteGuardado.getUsuario().getId(),
                clienteGuardado.getUsuario().getUsername()
        );
    }

    public ClienteResponse registrar(UsuarioClienteRequest request){

        if (clienteRepository.existsByDni(request.getDni())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese DNI");
        }

        if (clienteRepository.existsByCorreo(request.getDni())){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese correo");
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getUsername());
        usuario.setPassword(request.getPassword());
        usuario.setEstado("ACTIVO");
        usuario.setTipo_usuario(1);
        usuarioRepository.save(usuario);

        Cliente cliente= new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setDni(request.getDni());
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreo(request.getCorreo());
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");/*NUEVO, AMATEUR, FRECUENTE, VIP*/

        Cliente clienteGuardado = clienteRepository.save(cliente);

        return new ClienteResponse(
                clienteGuardado.getId(),
                clienteGuardado.getNombre(),
                clienteGuardado.getApellido(),
                clienteGuardado.getDni(),
                clienteGuardado.getTelefono(),
                clienteGuardado.getCorreo(),
                clienteGuardado.getEstado(),
                clienteGuardado.getTipo_cliente(),
                clienteGuardado.getFechaHora_registro(),
                clienteGuardado.getFechaHora_actualizacion(),

                clienteGuardado.getUsuario().getId(),
                clienteGuardado.getUsuario().getUsername()

        );
    }

    public List<ClienteResponse> listarTodos(){
        return clienteRepository.findAll().stream().map(cliente -> new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getEstado(),
                cliente.getTipo_cliente(),
                cliente.getFechaHora_registro(),
                cliente.getFechaHora_actualizacion(),

                cliente.getUsuario().getId(),
                cliente.getUsuario().getUsername()
        )).toList();
    }

    public ClienteResponse obtenerPorId(Long id){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Cliente con id: "+id+" no encontrado"));

        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getDni(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getEstado(),
                cliente.getTipo_cliente(),
                cliente.getFechaHora_registro(),
                cliente.getFechaHora_actualizacion(),

                cliente.getUsuario().getId(),
                cliente.getUsuario().getUsername()
        );
    }

    public ClienteResponse actualizar(Long id, UsuarioClienteRequest request){

        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Cliente con id: "+id+" no encontrado"));

        if (clienteRepository.existsByDniAndIdNot(request.getDni(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese DNI");
        }

        if (clienteRepository.existsByCorreoAndIdNot(request.getDni(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese DNI");
        }

        clienteExistente.setNombre(request.getNombre());
        clienteExistente.setApellido(request.getApellido());
        clienteExistente.setDni(request.getDni());
        clienteExistente.setTelefono(request.getTelefono());
        clienteExistente.setCorreo(request.getCorreo());

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);

        return new ClienteResponse(
                clienteActualizado.getId(),
                clienteActualizado.getNombre(),
                clienteActualizado.getApellido(),
                clienteActualizado.getDni(),
                clienteActualizado.getTelefono(),
                clienteActualizado.getCorreo(),
                clienteActualizado.getEstado(),
                clienteActualizado.getTipo_cliente(),
                clienteActualizado.getFechaHora_registro(),
                clienteActualizado.getFechaHora_actualizacion(),

                clienteActualizado.getUsuario().getId(),
                clienteActualizado.getUsuario().getUsername()
        );
    }

    public void eliminar(Long id){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(()-> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Cliente con id: "+id+" no encontrado"));
        clienteRepository.delete(cliente);
    }
}
