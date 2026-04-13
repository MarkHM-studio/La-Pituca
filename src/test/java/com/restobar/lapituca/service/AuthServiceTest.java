package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.auth.*;
import com.restobar.lapituca.dto.request.UsuarioClienteRequest;
import com.restobar.lapituca.entity.*;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.*;
import com.restobar.lapituca.security.jwt.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @InjectMocks
    private AuthService authService;

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private RolRepository rolRepository;
    @Mock
    private DistritoRepository distritoRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private PasswordRecoveryEmailService passwordRecoveryEmailService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        authService.frontendResetPasswordUrl = "http://localhost:5173/reset-password";
        authService.resetPasswordExpirationMinutes = 30L;
    }

    // ===================================
    // LOGIN
    // ===================================
    @Test
    void testLoginExitoso() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("user@dominio.com");
        request.setPassword("123");

        Usuario usuario = new Usuario();
        usuario.setEstado("ACTIVO");
        usuario.setProvider("LOCAL");
        usuario.setId(1L);
        Rol rol = new Rol();
        rol.setNombre("CLIENTE");
        usuario.setRol(rol);

        when(usuarioRepository.findByUsername("user@dominio.com")).thenReturn(Optional.of(usuario));
        // authenticate no es void, no usamos doNothing
        // solo dejamos que no lance excepción

        when(jwtService.generateToken(usuario)).thenReturn("TOKEN");
        when(usuarioRepository.save(usuario)).thenReturn(usuario);

        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("TOKEN", response.getToken());
        assertEquals(1L, response.getUsuarioId());
    }

    @Test
    void testLoginCuentaGoogle() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("google@user.com");
        request.setPassword("123");

        Usuario usuario = new Usuario();
        usuario.setProvider("GOOGLE");

        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
    }

    @Test
    void testLoginCuentaInactiva() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("inactive@user.com");
        request.setPassword("123");

        Usuario usuario = new Usuario();
        usuario.setProvider("LOCAL");
        usuario.setEstado("INACTIVO");

        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));

        ApiException ex = assertThrows(ApiException.class, () -> authService.login(request));
        assertEquals(ErrorCode.UNAUTHORIZED, ex.getErrorCode());
    }

    // ===================================
    // REGISTER CLIENTE
    // ===================================
    @Test
    void testRegisterClienteLocalExitoso() {
        UsuarioClienteRequest request = new UsuarioClienteRequest();
        request.setCorreo("cliente@dominio.com");
        request.setNombre("Nombre");
        request.setApellido("Apellido");
        request.setTelefono("123456789");
        request.setDistrito("Distrito");
        request.setPassword("123");
        request.setFechaNacimiento(LocalDate.of(1990, 1, 1));

        when(usuarioRepository.existsByUsername(anyString())).thenReturn(false);
        when(clienteRepository.existsByCorreo(anyString())).thenReturn(false);
        when(clienteRepository.existsByTelefono(anyString())).thenReturn(false);

        Rol rol = new Rol();
        rol.setNombre("CLIENTE");
        when(rolRepository.findByNombreIgnoreCase("CLIENTE")).thenReturn(Optional.of(rol));

        Distrito distrito = new Distrito();
        distrito.setNombre("Distrito");
        when(distritoRepository.save(any())).thenReturn(distrito);

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setId(1L);
        usuarioGuardado.setRol(rol);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);

        Cliente clienteGuardado = new Cliente();
        clienteGuardado.setId(1L);
        clienteGuardado.setUsuario(usuarioGuardado);
        when(clienteRepository.save(any())).thenReturn(clienteGuardado);
        when(jwtService.generateToken(usuarioGuardado)).thenReturn("TOKEN");

        RegisterResponse response = authService.registerClienteLocal(request);

        assertNotNull(response);
        assertEquals("TOKEN", response.getToken());
        assertEquals(1L, response.getUsuarioId());
        assertEquals(1L, response.getCliente().getId());
    }

    // ===================================
    // RESET PASSWORD
    // ===================================
    @Test
    void testRequestPasswordResetExitoso() {
        ForgotPasswordRequest request = new ForgotPasswordRequest();
        request.setCorreo("user@dominio.com");

        Usuario usuario = new Usuario();
        usuario.setProvider("LOCAL");
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordExpiry(null);

        Cliente cliente = new Cliente();
        cliente.setNombre("Nombre");

        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        when(clienteRepository.findByCorreo(anyString())).thenReturn(Optional.of(cliente));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedToken");

        authService.requestPasswordReset(request);

        verify(passwordRecoveryEmailService).sendPasswordRecoveryEmail(
                eq("user@dominio.com"), eq("Nombre"), anyString(), anyString(), any(LocalDateTime.class)
        );
    }

    @Test
    void testVerifyResetTokenExitoso() {
        Usuario usuario = new Usuario();
        usuario.setProvider("LOCAL"); // CORRECCIÓN: debe ser LOCAL
        usuario.setResetPasswordToken("encoded");
        usuario.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        VerifyResetTokenRequest request = new VerifyResetTokenRequest();
        request.setCorreo("user@dominio.com");
        request.setToken("123456");

        var response = authService.verifyResetToken(request);

        assertNotNull(response);
        assertEquals("user@dominio.com", response.getCorreo());
    }

    @Test
    void testResetPasswordExitoso() {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setCorreo("user@dominio.com");
        request.setPassword("123");
        request.setConfirmPassword("123");
        request.setToken("123456");

        Usuario usuario = new Usuario();
        usuario.setProvider("LOCAL");
        usuario.setResetPasswordToken("encoded");
        usuario.setResetPasswordExpiry(LocalDateTime.now().plusMinutes(10));

        when(usuarioRepository.findByUsername(anyString())).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(passwordEncoder.encode(anyString())).thenReturn("newPassword");

        var response = authService.resetPassword(request);

        assertNotNull(response);
        assertEquals("La contraseña se actualizó correctamente. Ahora inicia sesión normalmente.", response.getMessage());
    }

    // ===================================
    // GOOGLE LOGIN
    // ===================================
    @Test
    void testProcesarLoginGoogleUsuarioNuevo() {
        Rol rol = new Rol();
        rol.setNombre("CLIENTE");
        when(rolRepository.findByNombreIgnoreCase("CLIENTE")).thenReturn(Optional.of(rol));

        Usuario usuarioGuardado = new Usuario();
        usuarioGuardado.setRol(rol);
        when(usuarioRepository.save(any())).thenReturn(usuarioGuardado);
        when(clienteRepository.save(any())).thenReturn(new Cliente());
        when(jwtService.generateToken(any())).thenReturn("TOKEN");

        LoginResponse response = authService.procesarLoginGoogle("nuevo@user.com", "Nombre Apellido", "foto.png");

        assertNotNull(response);
        assertEquals("TOKEN", response.getToken());
    }
}