package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.auth.*;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final DateTimeFormatter TOKEN_EXPIRY_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final RolRepository rolRepository;
    private final DistritoRepository distritoRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordRecoveryEmailService passwordRecoveryEmailService;

    @Value("${app.auth.reset-password-expiration-minutes:30}")
    long resetPasswordExpirationMinutes;

    @Value("${app.frontend.reset-password-url:http://localhost:5173/reset-password}")
    String frontendResetPasswordUrl;

    public LoginResponse login(LoginRequest request) {
        String correo = normalizeEmail(request.getCorreo());

        Usuario usuario = usuarioRepository.findByUsername(correo).orElse(null);

        if (usuario != null && "GOOGLE".equalsIgnoreCase(usuario.getProvider())) {
            throw new ApiException(
                    ErrorCode.BUSINESS_RULE_ERROR,
                    "Esta cuenta fue creada con Google. Inicia sesión con Google."
            );
        }

        if (usuario != null && "INACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new ApiException(
                    ErrorCode.UNAUTHORIZED,
                    "Tu cuenta está inactiva. Contacta al administrador."
            );
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(correo, request.getPassword())
            );
        } catch (DisabledException ex) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Tu cuenta está inactiva. Contacta al administrador.");
        } catch (AuthenticationException ex) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Correo o contraseña incorrectos");
        }

        Usuario usuarioAuth = usuarioRepository.findByUsername(correo)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        validarUsuarioActivo(usuarioAuth);
        usuarioAuth.setUltimoLogin(LocalDateTime.now());
        usuarioRepository.save(usuarioAuth);

        return buildLoginResponse(usuarioAuth);
    }

    @Transactional
    public RegisterResponse registerClienteLocal(UsuarioClienteRequest request) {
        validarRegistroCliente(request);

        Rol rolCliente = rolRepository.findByNombreIgnoreCase("CLIENTE")
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No existe el rol CLIENTE"));

        Distrito distrito = distritoRepository.findByNombreIgnoreCase(request.getDistrito().trim())
                .orElseGet(() -> crearDistrito(request.getDistrito()));

        Usuario usuario = new Usuario();
        usuario.setUsername(normalizeEmail(request.getCorreo()));
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolCliente);
        usuario.setProvider("LOCAL");
        usuario.setTipo_usuario(1);
        usuario.setProveedorId(1);
        usuario.setEstado("ACTIVO");
        usuario.setUltimoLogin(LocalDateTime.now());
        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setNombre(request.getNombre().trim());
        cliente.setApellido(request.getApellido().trim());
        cliente.setFechaNacimiento(request.getFechaNacimiento());
        cliente.setCorreo(normalizeEmail(request.getCorreo()));
        cliente.setTelefono(request.getTelefono().trim());
        cliente.setDistrito(distrito);
        cliente.setEstado("ACTIVO");
        cliente.setTipo_cliente("NUEVO");
        cliente.setUsuario(usuarioGuardado);
        Cliente clienteGuardado = clienteRepository.save(cliente);

        LoginResponse login = buildLoginResponse(usuarioGuardado);
        return new RegisterResponse(
                login.getToken(),
                login.getUsuarioId(),
                login.getCorreo(),
                login.getRol(),
                login.getProveedor(),
                toClienteResponse(clienteGuardado)
        );
    }

    @Transactional
    public ForgotPasswordResponse requestPasswordReset(ForgotPasswordRequest request) {
        String correo = normalizeEmail(request.getCorreo());
        Usuario usuario = usuarioRepository.findByUsername(correo)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No existe una cuenta asociada a ese correo"));

        if (!"LOCAL".equalsIgnoreCase(usuario.getProvider())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Esta cuenta fue creada con Google. Inicia sesión con Google.");
        }

        Cliente cliente = clienteRepository.findByCorreo(correo)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Cliente no encontrado para el correo indicado"));

        String rawToken = generateReadableResetToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(resetPasswordExpirationMinutes);

        usuario.setResetPasswordToken(passwordEncoder.encode(rawToken));
        usuario.setResetPasswordExpiry(expiresAt);
        usuarioRepository.save(usuario);

        String resetLink = buildResetLink(correo, rawToken);
        passwordRecoveryEmailService.sendPasswordRecoveryEmail(
                correo,
                cliente.getNombre(),
                rawToken,
                resetLink,
                expiresAt
        );

        return new ForgotPasswordResponse(
                "Te enviamos un correo con el código y enlace de recuperación. Revisa tu bandeja de entrada."
        );
    }

    public VerifyResetTokenResponse verifyResetToken(VerifyResetTokenRequest request) {
        Usuario usuario = findUsuarioForReset(request.getCorreo());
        assertValidResetToken(usuario, request.getToken());

        return new VerifyResetTokenResponse(
                "Código verificado correctamente. Ya puedes definir tu nueva contraseña.",
                normalizeEmail(request.getCorreo()),
                usuario.getResetPasswordExpiry().format(TOKEN_EXPIRY_FORMATTER)
        );
    }

    @Transactional
    public PasswordResetResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "La confirmación de contraseña no coincide");
        }

        Usuario usuario = findUsuarioForReset(request.getCorreo());
        assertValidResetToken(usuario, request.getToken());

        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordExpiry(null);
        usuarioRepository.save(usuario);

        return new PasswordResetResponse("La contraseña se actualizó correctamente. Ahora inicia sesión normalmente.");
    }

    public AuthMeResponse getAuthenticatedUser(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "Usuario no encontrado"));

        Cliente cliente = clienteRepository.findByCorreo(usuario.getUsername()).orElse(null);
        String nombreCompleto = cliente == null ? null : (cliente.getNombre() + " " + cliente.getApellido()).trim();

        return new AuthMeResponse(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol().getNombre(),
                usuario.getProvider(),
                usuario.getEstado(),
                cliente != null ? cliente.getId() : null,
                nombreCompleto,
                usuario.getFoto()
        );
    }

    @Transactional
    public LoginResponse procesarLoginGoogle(String email, String fullName, String picture) {
        if (email == null || email.isBlank()) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Google no devolvió un correo válido");
        }

        String emailNormalizado = normalizeEmail(email);
        Usuario usuario = usuarioRepository.findByUsername(emailNormalizado)
                .orElseGet(() -> crearUsuarioGoogle(emailNormalizado, fullName, picture));
        validarUsuarioActivo(usuario);

        usuario.setUltimoLogin(LocalDateTime.now());
        if (usuario.getFoto() == null && picture != null) {
            usuario.setFoto(picture);
        }
        usuarioRepository.save(usuario);

        clienteRepository.findByCorreo(emailNormalizado)
                .orElseGet(() -> crearClienteGoogle(emailNormalizado, fullName, usuario));

        return buildLoginResponse(usuario);
    }

    private void validarRegistroCliente(UsuarioClienteRequest request) {
        String correo = normalizeEmail(request.getCorreo());
        String telefono = request.getTelefono().trim();

        if (usuarioRepository.existsByUsername(correo)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un usuario registrado con ese correo");
        }

        if (clienteRepository.existsByCorreo(correo)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un cliente registrado con ese correo");
        }

        if (clienteRepository.existsByTelefono(telefono)) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Ya existe un cliente registrado con ese teléfono");
        }
    }

    private Usuario findUsuarioForReset(String correo) {
        String correoNormalizado = normalizeEmail(correo);
        Usuario usuario = usuarioRepository.findByUsername(correoNormalizado)
                .orElseThrow(() -> new ApiException(ErrorCode.RESOURCE_NOT_FOUND, "No existe una cuenta asociada a ese correo"));

        if (!"LOCAL".equalsIgnoreCase(usuario.getProvider())) {
            throw new ApiException(ErrorCode.BUSINESS_RULE_ERROR, "Esta cuenta fue creada con Google. Inicia sesión con Google.");
        }
        return usuario;
    }

    private void validarUsuarioActivo(Usuario usuario) {
        if ("INACTIVO".equalsIgnoreCase(usuario.getEstado())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "Tu cuenta está inactiva. Contacta al administrador.");
        }
    }

    private void assertValidResetToken(Usuario usuario, String rawToken) {
        if (usuario.getResetPasswordToken() == null || usuario.getResetPasswordExpiry() == null) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "No existe un proceso de recuperación activo para este usuario");
        }

        if (usuario.getResetPasswordExpiry().isBefore(LocalDateTime.now())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "El código de recuperación ha expirado");
        }

        if (!passwordEncoder.matches(rawToken.trim(), usuario.getResetPasswordToken())) {
            throw new ApiException(ErrorCode.UNAUTHORIZED, "El código de recuperación no es válido");
        }
    }

    private String buildResetLink(String correo, String token) {
        return UriComponentsBuilder.fromUriString(frontendResetPasswordUrl)
                .queryParam("email", correo)
                .queryParam("token", token)
                .build()
                .toUriString();
    }

    private String generateReadableResetToken() {
        int code = 100000 + SECURE_RANDOM.nextInt(900000);
        return String.valueOf(code);
    }

    private String normalizeEmail(String correo) {
        return correo.trim().toLowerCase();
    }

    private Distrito crearDistrito(String nombreDistrito) {
        Distrito distrito = new Distrito();
        distrito.setNombre(nombreDistrito.trim());
        return distritoRepository.save(distrito);
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