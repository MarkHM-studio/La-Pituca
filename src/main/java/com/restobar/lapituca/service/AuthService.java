package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.auth.LoginRequest;
import com.restobar.lapituca.dto.auth.LoginResponse;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.entity.Cliente;
import com.restobar.lapituca.entity.Distrito;
import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ClienteRepository;
import com.restobar.lapituca.repository.DistritoRepository;
import com.restobar.lapituca.repository.RolRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import com.restobar.lapituca.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final DistritoRepository distritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getCorreo(), request.getPassword())
            );
        } catch (AuthenticationException ex) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Correo o contraseña inválidos");
        }

        Usuario usuario = usuarioRepository.findByUsername(request.getCorreo())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        usuario.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuario);

        return buildLoginResponse(usuario);
    }

    public ClienteResponse registerClienteLocal(UsuarioClienteRequest request) {
        if (usuarioRepository.existsByUsername(request.getCorreo())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un usuario registrado con ese correo");
        }

        if (clienteRepository.existsByCorreo(request.getCorreo())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un cliente registrado con ese correo");
        }

        if (clienteRepository.existsByTelefono(request.getTelefono())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un cliente registrado con ese teléfono");
        }

        Rol rolCliente = rolRepository.findByNombreIgnoreCase("ADMINISTRADOR")
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No existe el rol CLIENTE"));

        Distrito distrito = distritoRepository.findByNombreIgnoreCase(request.getDistrito())
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Distrito no encontrado: " + request.getDistrito()));

        Usuario usuario = new Usuario();
        usuario.setUsername(request.getCorreo());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolCliente);
        usuario.setProvider("LOCAL");
        usuario.setTipo_usuario(1);
        usuario.setProveedorId(1);
        usuario.setEstado("ACTIVO");
        usuario.setUltimoLogin(LocalDateTime.now());
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre());
        cliente.setApellido(request.getApellido());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCorreo(request.getCorreo());
        cliente.setTelefono(request.getTelefono());
        cliente.setDistrito(distrito);
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");
        cliente.setUsuario(usuarioGuardado);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        return toClienteResponse(clienteGuardado);
    }

    public LoginResponse procesarLoginGoogle(String email, String fullName, String picture) {
        if (email == null || email.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Google no devolvió un correo válido");
        }

        Usuario usuario = usuarioRepository.findByUsername(email).orElseGet(() -> crearUsuarioGoogle(email, fullName, picture));

        usuario.setUltimoLogin(LocalDateTime.now());
        if (usuario.getFoto() == null && picture != null) {
            usuario.setFoto(picture);
        }
        usuarioRepository.save(usuario);

        clienteRepository.findByCorreo(email).orElseGet(() -> crearClienteGoogle(email, fullName, usuario));

        return buildLoginResponse(usuario);
    }

    private Usuario crearUsuarioGoogle(String email, String fullName, String picture) {
        Rol rolCliente = rolRepository.findByNombreIgnoreCase("CLIENTE")
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No existe el rol CLIENTE"));

        Usuario usuario = new Usuario();
        usuario.setUsername(email);
        usuario.setPassword(null);
        usuario.setRol(rolCliente);
        usuario.setProvider("GOOGLE");
        usuario.setTipo_usuario(1);
        usuario.setProveedorId(2);
        usuario.setFoto(picture);
        usuario.setEstado("ACTIVO");
        usuario.setUltimoLogin(LocalDateTime.now());
        return usuarioRepository.save(usuario);
    }

    private Cliente crearClienteGoogle(String email, String fullName, Usuario usuario) {
        String[] names = splitName(fullName);

        Cliente cliente = new Cliente();
        cliente.setNombre(names[0]);
        cliente.setApellido(names[1]);
        cliente.setCorreo(email);
        cliente.setTelefono(null);
        cliente.setFechaNacimiento(null);
        cliente.setDistrito(null);
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");
        cliente.setUsuario(usuario);
        return clienteRepository.save(cliente);
    }

    private String[] splitName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"Google", "User"};
        }
        String[] chunks = fullName.trim().split("\\s+");
        if (chunks.length == 1) {
            return new String[]{chunks[0], "SinApellido"};
        }
        String nombre = chunks[0];
        String apellido = String.join(" ", Arrays.copyOfRange(chunks, 1, chunks.length));
        return new String[]{nombre, apellido};
    }

    private LoginResponse buildLoginResponse(Usuario usuario) {
        String token = jwtService.generateToken(usuario);
        return new LoginResponse(token, usuario.getId(), usuario.getUsername(), usuario.getRol().getNombre(), usuario.getProvider());
    }

    private ClienteResponse toClienteResponse(Cliente cliente) {
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