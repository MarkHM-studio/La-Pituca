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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private ClienteRepository clienteRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private DistritoRepository distritoRepository;

    @InjectMocks private ClienteService clienteService;

    @Test
    void guardar_deberiaCrearClienteConValoresPorDefecto() {
        ClienteRequest request = requestBase();
        Usuario usuario = usuario(10L, "cliente_user", "ACTIVO");
        Distrito distrito = distrito(20L, "Miraflores");

        when(clienteRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(clienteRepository.existsByTelefono("999888777")).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(distritoRepository.findByNombreIgnoreCase("Miraflores")).thenReturn(Optional.of(distrito));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> {
            Cliente c = inv.getArgument(0);
            c.setId(100L);
            return c;
        });

        ClienteResponse response = clienteService.guardar(request);

        assertEquals(100L, response.getId());
        assertEquals("ACTIVO", response.getEstado());
        assertEquals("NUEVO", response.getTipoCliente());
        assertEquals("Miraflores", response.getDistrito());
        assertEquals(10L, response.getUsuarioId());
    }

    @Test
    void guardar_deberiaFallarSiCorreoYaExiste() {
        ClienteRequest request = requestBase();
        when(clienteRepository.existsByCorreo("ana@correo.com")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("ese Correo"));
        verify(usuarioRepository, never()).findById(anyLong());
    }

    @Test
    void guardar_deberiaFallarSiTelefonoYaExiste() {
        ClienteRequest request = requestBase();
        when(clienteRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(clienteRepository.existsByTelefono("999888777")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("ese teléfono"));
    }

    @Test
    void guardar_deberiaFallarSiDistritoNoExiste() {
        ClienteRequest request = requestBase();
        when(clienteRepository.existsByCorreo("ana@correo.com")).thenReturn(false);
        when(clienteRepository.existsByTelefono("999888777")).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario(10L, "cliente_user", "ACTIVO")));
        when(distritoRepository.findByNombreIgnoreCase("Miraflores")).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void listarTodos_deberiaFiltrarPorEstado() {
        Cliente activo = cliente(1L, "ACTIVO", usuario(10L, "u1", "ACTIVO"), distrito(20L, "Miraflores"));
        Cliente inactivo = cliente(2L, "INACTIVO", usuario(11L, "u2", "INACTIVO"), distrito(21L, "Surco"));

        when(clienteRepository.findAll()).thenReturn(List.of(activo, inactivo));

        List<ClienteResponse> response = clienteService.listarTodos("  activo ");

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("ACTIVO", response.get(0).getEstado());
    }

    @Test
    void obtenerPorId_deberiaRetornarCliente() {
        Cliente cliente = cliente(1L, "ACTIVO", usuario(10L, "u1", "ACTIVO"), distrito(20L, "Miraflores"));
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        ClienteResponse response = clienteService.obtenerPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Ana", response.getNombre());
        assertEquals("Miraflores", response.getDistrito());
    }

    @Test
    void obtenerPorId_deberiaFallarSiNoExiste() {
        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.obtenerPorId(999L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void actualizar_deberiaActualizarDatosYDistrito() {
        ClienteRequest request = new ClienteRequest(
                "Ana Maria",
                "Perez",
                LocalDate.of(1998, 5, 15),
                "999111222",
                "ana.nueva@correo.com",
                "Surco",
                10L
        );

        Cliente existente = cliente(50L, "ACTIVO", usuario(10L, "cliente_user", "ACTIVO"), distrito(20L, "Miraflores"));
        Distrito distritoNuevo = distrito(21L, "Surco");

        when(clienteRepository.findById(50L)).thenReturn(Optional.of(existente));
        when(clienteRepository.existsByCorreoAndIdNot("ana.nueva@correo.com", 50L)).thenReturn(false);
        when(clienteRepository.existsByTelefonoAndIdNot("999111222", 50L)).thenReturn(false);
        when(distritoRepository.findByNombreIgnoreCase("Surco")).thenReturn(Optional.of(distritoNuevo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        ClienteResponse response = clienteService.actualizar(50L, request);

        assertEquals(50L, response.getId());
        assertEquals("Ana Maria", response.getNombre());
        assertEquals("Surco", response.getDistrito());
        assertEquals("999111222", response.getTelefono());
    }

    @Test
    void actualizar_deberiaFallarSiCorreoDuplicado() {
        ClienteRequest request = requestBase();
        when(clienteRepository.findById(50L)).thenReturn(Optional.of(new Cliente()));
        when(clienteRepository.existsByCorreoAndIdNot("ana@correo.com", 50L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.actualizar(50L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
    }

    @Test
    void actualizar_deberiaFallarSiTelefonoDuplicado() {
        ClienteRequest request = requestBase();
        when(clienteRepository.findById(50L)).thenReturn(Optional.of(new Cliente()));
        when(clienteRepository.existsByCorreoAndIdNot("ana@correo.com", 50L)).thenReturn(false);
        when(clienteRepository.existsByTelefonoAndIdNot("999888777", 50L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> clienteService.actualizar(50L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
    }

    @Test
    void eliminar_deberiaInactivarClienteYUsuario() {
        Usuario usuario = usuario(10L, "cliente_user", "ACTIVO");
        Cliente cliente = cliente(1L, "ACTIVO", usuario, distrito(20L, "Miraflores"));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        clienteService.eliminar(1L);

        assertEquals("INACTIVO", cliente.getEstado());
        assertEquals("INACTIVO", usuario.getEstado());
        verify(clienteRepository).save(cliente);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void activar_deberiaActivarClienteYUsuario() {
        Usuario usuario = usuario(10L, "cliente_user", "INACTIVO");
        Cliente cliente = cliente(1L, "INACTIVO", usuario, distrito(20L, "Miraflores"));

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        clienteService.activar(1L);

        assertEquals("ACTIVO", cliente.getEstado());
        assertEquals("ACTIVO", usuario.getEstado());
        verify(clienteRepository).save(cliente);
        verify(usuarioRepository, never()).save(any());
    }

    private ClienteRequest requestBase() {
        return new ClienteRequest(
                "Ana",
                "Perez",
                LocalDate.of(2000, 1, 10),
                "999888777",
                "ana@correo.com",
                "Miraflores",
                10L
        );
    }

    private Usuario usuario(Long id, String username, String estado) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setEstado(estado);
        return u;
    }

    private Distrito distrito(Long id, String nombre) {
        Distrito d = new Distrito();
        d.setId(id);
        d.setNombre(nombre);
        return d;
    }

    private Cliente cliente(Long id, String estado, Usuario usuario, Distrito distrito) {
        Cliente c = new Cliente();
        c.setId(id);
        c.setNombre("Ana");
        c.setApellido("Perez");
        c.setFechaNacimiento(LocalDate.of(2000, 1, 10));
        c.setTelefono("999888777");
        c.setCorreo("ana@correo.com");
        c.setEstado(estado);
        c.setTipo_cliente("NUEVO");
        c.setUsuario(usuario);
        c.setDistrito(distrito);
        return c;
    }
}