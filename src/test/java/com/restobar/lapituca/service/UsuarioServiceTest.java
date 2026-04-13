package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.UsuarioRequest;
import com.restobar.lapituca.dto.response.UsuarioResponse;
import com.restobar.lapituca.entity.Cliente;
import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.entity.Trabajador;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.ClienteRepository;
import com.restobar.lapituca.repository.RolRepository;
import com.restobar.lapituca.repository.TrabajadorRepository;
import com.restobar.lapituca.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TrabajadorRepository trabajadorRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void guardar_deberiaGuardarYRetornarResponse_cuandoRequestEsValido() {
        UsuarioRequest request = new UsuarioRequest("usuario.test", "P@ssword123", 2L);
        Rol rol = crearRol(2L, "ADMIN");

        when(rolRepository.findById(2L)).thenReturn(Optional.of(rol));
        when(usuarioRepository.existsByUsername("usuario.test")).thenReturn(false);
        when(passwordEncoder.encode("P@ssword123")).thenReturn("encoded-password");

        UsuarioResponse response = usuarioService.guardar(request);

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        Usuario guardado = captor.getValue();

        assertEquals("usuario.test", guardado.getUsername());
        assertEquals("encoded-password", guardado.getPassword());
        assertEquals("ACTIVO", guardado.getEstado());
        assertEquals("LOCAL", guardado.getProvider());
        assertEquals(1, guardado.getProveedorId());
        assertEquals(1, guardado.getTipo_usuario());
        assertEquals(rol, guardado.getRol());

        assertEquals("usuario.test", response.getUsername());
        assertEquals("ACTIVO", response.getEstado());
        assertEquals(1, response.getTipo_usuario());
        assertEquals(2L, response.getRolId());
        assertEquals("ADMIN", response.getRolNombre());
    }

    @Test
    void guardar_deberiaLanzarApiException_cuandoNoExisteRol() {
        UsuarioRequest request = new UsuarioRequest("usuario.test", "P@ssword123", 9L);
        when(rolRepository.findById(9L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Rol con id: 9 no encontrado"));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void guardar_deberiaLanzarApiException_cuandoUsernameYaExiste() {
        UsuarioRequest request = new UsuarioRequest("usuario.test", "P@ssword123", 2L);
        Rol rol = crearRol(2L, "ADMIN");

        when(rolRepository.findById(2L)).thenReturn(Optional.of(rol));
        when(usuarioRepository.existsByUsername("usuario.test")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertEquals("Ya existe un Usuario con este nombre", ex.getMessage());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void listarTodos_deberiaRetornarTodos_cuandoEstadoEsNull() {
        Usuario activo = crearUsuario(1L, "activo.user", "ACTIVO", 2, crearRol(2L, "ADMIN"));
        Usuario inactivo = crearUsuario(2L, "inactivo.user", "INACTIVO", 1, crearRol(1L, "CAJERO"));

        when(usuarioRepository.findAll()).thenReturn(List.of(activo, inactivo));

        List<UsuarioResponse> resultado = usuarioService.listarTodos(null);

        assertEquals(2, resultado.size());
        assertEquals("activo.user", resultado.get(0).getUsername());
        assertEquals("ADMIN", resultado.get(0).getRolNombre());
        assertEquals("inactivo.user", resultado.get(1).getUsername());
        assertEquals("CAJERO", resultado.get(1).getRolNombre());
    }

    @Test
    void listarTodos_deberiaFiltrarPorEstadoIgnorandoEspaciosYMayusculas() {
        Usuario activo = crearUsuario(1L, "activo.user", "ACTIVO", 2, crearRol(2L, "ADMIN"));
        Usuario inactivo = crearUsuario(2L, "inactivo.user", "INACTIVO", 1, crearRol(1L, "CAJERO"));

        when(usuarioRepository.findAll()).thenReturn(List.of(activo, inactivo));

        List<UsuarioResponse> resultado = usuarioService.listarTodos("  activo ");

        assertEquals(1, resultado.size());
        assertEquals("activo.user", resultado.get(0).getUsername());
        assertEquals("ACTIVO", resultado.get(0).getEstado());
    }

    @Test
    void obtenerPorId_deberiaRetornarUsuarioResponse_cuandoExiste() {
        Usuario usuario = crearUsuario(10L, "usuario.id", "ACTIVO", 1, crearRol(5L, "MOZO"));

        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        UsuarioResponse response = usuarioService.obtenerPorId(10L);

        assertEquals(10L, response.getId());
        assertEquals("usuario.id", response.getUsername());
        assertEquals(5L, response.getRolId());
        assertEquals("MOZO", response.getRolNombre());
        assertNotNull(response.getFechaHora_registro());
        assertNotNull(response.getFechaHora_actualizacion());
    }

    @Test
    void obtenerPorId_deberiaLanzarApiException_cuandoNoExiste() {
        when(usuarioRepository.findById(100L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.obtenerPorId(100L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Usuario con id: 100 no encontrada"));
    }

    @Test
    void actualizar_deberiaActualizarUsuarioConDefaults_cuandoCamposNulos() {
        UsuarioRequest request = new UsuarioRequest("nuevo.user", "N3wP@ssword", 3L);
        Rol rolNuevo = crearRol(3L, "SUPERVISOR");

        Usuario existente = crearUsuario(5L, "viejo.user", "ACTIVO", null, crearRol(1L, "CAJERO"));
        existente.setProveedorId(null);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByUsernameAndIdNot("nuevo.user", 5L)).thenReturn(false);
        when(rolRepository.findById(3L)).thenReturn(Optional.of(rolNuevo));
        when(passwordEncoder.encode("N3wP@ssword")).thenReturn("encoded-new");

        UsuarioResponse response = usuarioService.actualizar(5L, request);

        verify(usuarioRepository).save(existente);
        assertEquals("nuevo.user", existente.getUsername());
        assertEquals("encoded-new", existente.getPassword());
        assertEquals(rolNuevo, existente.getRol());
        assertEquals(1, existente.getProveedorId());
        assertEquals(1, existente.getTipo_usuario());

        assertEquals("nuevo.user", response.getUsername());
        assertEquals("SUPERVISOR", response.getRolNombre());
        assertEquals(1, response.getTipo_usuario());
    }

    @Test
    void actualizar_deberiaMantenerValoresExistentes_cuandoNoSonNulos() {
        UsuarioRequest request = new UsuarioRequest("usuario.actualizado", "N3wP@ssword", 2L);
        Rol rolNuevo = crearRol(2L, "ADMIN");
        Usuario existente = crearUsuario(5L, "usuario.original", "ACTIVO", 9, crearRol(1L, "CAJERO"));
        existente.setProveedorId(7);

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByUsernameAndIdNot("usuario.actualizado", 5L)).thenReturn(false);
        when(rolRepository.findById(2L)).thenReturn(Optional.of(rolNuevo));
        when(passwordEncoder.encode("N3wP@ssword")).thenReturn("encoded-new");

        usuarioService.actualizar(5L, request);

        assertEquals(7, existente.getProveedorId());
        assertEquals(9, existente.getTipo_usuario());
    }

    @Test
    void actualizar_deberiaLanzarError_cuandoUsuarioNoExiste() {
        UsuarioRequest request = new UsuarioRequest("nuevo.user", "N3wP@ssword", 3L);
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.actualizar(99L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarErrorNegocio_cuandoUsernameDuplicado() {
        UsuarioRequest request = new UsuarioRequest("duplicado.user", "N3wP@ssword", 3L);
        Usuario existente = crearUsuario(5L, "viejo.user", "ACTIVO", 1, crearRol(1L, "CAJERO"));

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByUsernameAndIdNot("duplicado.user", 5L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.actualizar(5L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        verify(rolRepository, never()).findById(any());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void actualizar_deberiaLanzarError_cuandoRolNoExiste() {
        UsuarioRequest request = new UsuarioRequest("nuevo.user", "N3wP@ssword", 33L);
        Usuario existente = crearUsuario(5L, "viejo.user", "ACTIVO", 1, crearRol(1L, "CAJERO"));

        when(usuarioRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(usuarioRepository.existsByUsernameAndIdNot("nuevo.user", 5L)).thenReturn(false);
        when(rolRepository.findById(33L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.actualizar(5L, request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaInactivarUsuarioTrabajadorYCliente_cuandoExistenRelacionados() {
        Usuario usuario = crearUsuario(12L, "eliminar.user", "ACTIVO", 1, crearRol(1L, "CAJERO"));
        Trabajador trabajador = new Trabajador();
        trabajador.setEstado("ACTIVO");
        Cliente cliente = new Cliente();
        cliente.setEstado("ACTIVO");

        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(trabajadorRepository.findByUsuario(usuario)).thenReturn(Optional.of(trabajador));
        when(clienteRepository.findByUsuario(usuario)).thenReturn(Optional.of(cliente));

        usuarioService.eliminar(12L);

        assertEquals("INACTIVO", usuario.getEstado());
        assertEquals("INACTIVO", trabajador.getEstado());
        assertEquals("INACTIVO", cliente.getEstado());
        verify(usuarioRepository).save(usuario);
        verify(trabajadorRepository).save(trabajador);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void eliminar_deberiaSoloInactivarUsuario_cuandoNoHayRelacionados() {
        Usuario usuario = crearUsuario(12L, "eliminar.user", "ACTIVO", 1, crearRol(1L, "CAJERO"));

        when(usuarioRepository.findById(12L)).thenReturn(Optional.of(usuario));
        when(trabajadorRepository.findByUsuario(usuario)).thenReturn(Optional.empty());
        when(clienteRepository.findByUsuario(usuario)).thenReturn(Optional.empty());

        usuarioService.eliminar(12L);

        assertEquals("INACTIVO", usuario.getEstado());
        verify(usuarioRepository).save(usuario);
        verify(trabajadorRepository, never()).save(any());
        verify(clienteRepository, never()).save(any());
    }

    @Test
    void eliminar_deberiaLanzarError_cuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(120L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.eliminar(120L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    void activar_deberiaActivarUsuarioTrabajadorYCliente_cuandoExistenRelacionados() {
        Usuario usuario = crearUsuario(13L, "activar.user", "INACTIVO", 1, crearRol(1L, "CAJERO"));
        Trabajador trabajador = new Trabajador();
        trabajador.setEstado("INACTIVO");
        Cliente cliente = new Cliente();
        cliente.setEstado("INACTIVO");

        when(usuarioRepository.findById(13L)).thenReturn(Optional.of(usuario));
        when(trabajadorRepository.findByUsuario(usuario)).thenReturn(Optional.of(trabajador));
        when(clienteRepository.findByUsuario(usuario)).thenReturn(Optional.of(cliente));

        usuarioService.activar(13L);

        assertEquals("ACTIVO", usuario.getEstado());
        assertEquals("ACTIVO", trabajador.getEstado());
        assertEquals("ACTIVO", cliente.getEstado());
        verify(usuarioRepository).save(usuario);
        verify(trabajadorRepository).save(trabajador);
        verify(clienteRepository).save(cliente);
    }

    @Test
    void activar_deberiaLanzarError_cuandoUsuarioNoExiste() {
        when(usuarioRepository.findById(130L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> usuarioService.activar(130L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Usuario con id: 130 no encontrado"));
        verify(usuarioRepository, never()).save(any());
    }

    private Usuario crearUsuario(Long id, String username, String estado, Integer tipoUsuario, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setUsername(username);
        usuario.setPassword("encoded");
        usuario.setEstado(estado);
        usuario.setProvider("LOCAL");
        usuario.setProveedorId(1);
        usuario.setTipo_usuario(tipoUsuario);
        usuario.setRol(rol);
        usuario.setFechaHora_registro(LocalDateTime.of(2026, 1, 10, 10, 0));
        usuario.setFechaHora_actualizacion(LocalDateTime.of(2026, 1, 12, 11, 30));
        return usuario;
    }

    private Rol crearRol(Long id, String nombre) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        return rol;
    }
}