package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.ClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.entity.Cliente;
import com.restobar.lapituca.entity.Distrito;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ClienteRepository;
import com.restobar.lapituca.repository.DistritoRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final DistritoRepository distritoRepository;

    public ClienteResponse guardar(ClienteRequest request) {

        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Cliente con ese Correo");
        }

        if (clienteRepository.existsByTelefono(request.getTelefono())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un Cliente con ese teléfono");
        }

        Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Usuario con id: " + request.getUsuarioId() + " no encontrado"));

        Distrito distrito = distritoRepository.findByNombreIgnoreCase(request.getDistrito())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Distrito con nombre: " + request.getDistrito() + " no encontrado"));

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setTelefono(request.getTelefono());
        cliente.setCorreo(request.getCorreo());
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");
        cliente.setUsuario(usuario);
        cliente.setDistrito(distrito);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return toResponse(clienteGuardado);
    }

    public List<ClienteResponse> listarTodos(){
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    public ClienteResponse obtenerPorId(Long id){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Cliente con id: " + id + " no encontrado"));
        return toResponse(cliente);
    }

    public ClienteResponse actualizar(Long id, ClienteRequest request){

        Cliente clienteExistente = clienteRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Cliente con id: " + id + " no encontrado"));

        if (clienteRepository.existsByCorreoAndIdNot(request.getCorreo(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese correo");
        }

        if (clienteRepository.existsByTelefonoAndIdNot(request.getTelefono(), id)){
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR,"Ya existe un Cliente con ese teléfono");
        }

        Distrito distrito = distritoRepository.findByNombreIgnoreCase(request.getDistrito())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND,"Distrito con nombre: " + request.getDistrito() + " no encontrado"));

        clienteExistente.setNombre(request.getNombre());
        clienteExistente.setApellido(request.getApellido());
        clienteExistente.setFechaNacimiento(request.getFechaNacimiento());
        clienteExistente.setTelefono(request.getTelefono());
        clienteExistente.setCorreo(request.getCorreo());
        clienteExistente.setDistrito(distrito);

        Cliente clienteActualizado = clienteRepository.save(clienteExistente);

        return toResponse(clienteActualizado);
    }

    public void eliminar(Long id){
        Cliente cliente = clienteRepository.findById(id).orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Cliente con id: " + id + " no encontrado"));
        clienteRepository.delete(cliente);
    }

    private ClienteResponse toResponse(Cliente cliente) {
        return new ClienteResponse(
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellido(),
                cliente.getFechaNacimiento(),
                cliente.getTelefono(),
                cliente.getCorreo(),
                cliente.getEstado(),
                cliente.getTipo_cliente(),
                cliente.getDistrito() != null ? cliente.getDistrito().getNombre() : null,
                cliente.getFechaHora_registro(),
                cliente.getFechaHora_actualizacion(),
                cliente.getUsuario().getId(),
                cliente.getUsuario().getUsername()
        );
    }
}