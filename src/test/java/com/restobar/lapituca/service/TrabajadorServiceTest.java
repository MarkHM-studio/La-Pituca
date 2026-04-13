package com.restobar.lapituca.service;

import com.restobar.lapituca.dto.request.TrabajadorRequest;
import com.restobar.lapituca.dto.response.TrabajadorResponse;
import com.restobar.lapituca.entity.TipoJornada;
import com.restobar.lapituca.entity.Trabajador;
import com.restobar.lapituca.entity.Turno;
import com.restobar.lapituca.entity.Usuario;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.exception.ErrorCode;
import com.restobar.lapituca.repository.TipoJornadaRepository;
import com.restobar.lapituca.repository.TrabajadorRepository;
import com.restobar.lapituca.repository.TurnoRepository;
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
class TrabajadorServiceTest {

    @Mock private TrabajadorRepository trabajadorRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private TurnoRepository turnoRepository;
    @Mock private TipoJornadaRepository tipoJornadaRepository;

    @InjectMocks private TrabajadorService trabajadorService;

    @Test
    void guardar_deberiaCrearTrabajadorYActualizarTipoUsuario() {
        TrabajadorRequest request = requestBase();

        Usuario usuario = usuario(10L, "juan", "ACTIVO", 1);
        Turno turno = turno(20L, "Noche");
        TipoJornada tipoJornada = tipoJornada(30L, "Completa");

        when(trabajadorRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(turnoRepository.findById(20L)).thenReturn(Optional.of(turno));
        when(tipoJornadaRepository.findById(30L)).thenReturn(Optional.of(tipoJornada));
        when(trabajadorRepository.save(any(Trabajador.class))).thenAnswer(inv -> {
            Trabajador t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        TrabajadorResponse response = trabajadorService.guardar(request);

        assertEquals(100L, response.getId());
        assertEquals("Juan Carlos", response.getNombre());
        assertEquals("ACTIVO", response.getEstado());
        assertEquals(2, usuario.getTipo_usuario());

        verify(usuarioRepository).save(usuario);
        verify(trabajadorRepository).save(any(Trabajador.class));
    }

    @Test
    void guardar_deberiaFallarSiDniYaExiste() {
        TrabajadorRequest request = requestBase();
        when(trabajadorRepository.existsByDni("12345678")).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> trabajadorService.guardar(request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
        assertTrue(ex.getMessage().contains("Ya existe un Trabajador con ese DNI"));
        verify(usuarioRepository, never()).findById(anyLong());
    }

    @Test
    void guardar_deberiaFallarSiUsuarioNoExiste() {
        TrabajadorRequest request = requestBase();
        when(trabajadorRepository.existsByDni("12345678")).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> trabajadorService.guardar(request));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void listarTodos_deberiaFiltrarPorEstadoIgnorandoMayusculasYEspacios() {
        Trabajador activo = trabajador(1L, "ACTIVO", usuario(10L, "u1", "ACTIVO", 2), turno(20L, "Noche"), tipoJornada(30L, "Completa"));
        Trabajador inactivo = trabajador(2L, "INACTIVO", usuario(11L, "u2", "INACTIVO", 2), turno(21L, "Dia"), tipoJornada(31L, "Parcial"));

        when(trabajadorRepository.findAll()).thenReturn(List.of(activo, inactivo));

        List<TrabajadorResponse> response = trabajadorService.listarTodos("  activo ");

        assertEquals(1, response.size());
        assertEquals(1L, response.get(0).getId());
        assertEquals("ACTIVO", response.get(0).getEstado());
    }

    @Test
    void obtenerPorId_deberiaRetornarTrabajador() {
        Trabajador trabajador = trabajador(1L, "ACTIVO", usuario(10L, "u1", "ACTIVO", 2), turno(20L, "Noche"), tipoJornada(30L, "Completa"));
        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        TrabajadorResponse response = trabajadorService.obtenerPorId(1L);

        assertEquals(1L, response.getId());
        assertEquals("Juan Carlos", response.getNombre());
    }

    @Test
    void obtenerPorId_deberiaFallarSiNoExiste() {
        when(trabajadorRepository.findById(999L)).thenReturn(Optional.empty());

        ApiException ex = assertThrows(ApiException.class, () -> trabajadorService.obtenerPorId(999L));

        assertEquals(ErrorCode.RESOURCE_NOT_FOUND, ex.getErrorCode());
    }

    @Test
    void actualizar_deberiaActualizarTrabajador() {
        TrabajadorRequest request = requestBase();
        Trabajador existente = trabajador(50L, "INACTIVO", usuario(10L, "old", "ACTIVO", 2), turno(21L, "Dia"), tipoJornada(31L, "Parcial"));

        Usuario usuario = usuario(10L, "juan", "ACTIVO", 2);
        Turno turno = turno(20L, "Noche");
        TipoJornada tipoJornada = tipoJornada(30L, "Completa");

        when(trabajadorRepository.findById(50L)).thenReturn(Optional.of(existente));
        when(trabajadorRepository.existsByDniAndIdNot("12345678", 50L)).thenReturn(false);
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));
        when(turnoRepository.findById(20L)).thenReturn(Optional.of(turno));
        when(tipoJornadaRepository.findById(30L)).thenReturn(Optional.of(tipoJornada));
        when(trabajadorRepository.save(any(Trabajador.class))).thenAnswer(inv -> inv.getArgument(0));

        TrabajadorResponse response = trabajadorService.actualizar(50L, request);

        assertEquals(50L, response.getId());
        assertEquals("ACTIVO", response.getEstado());
        assertEquals("12345678", response.getDni());
    }

    @Test
    void actualizar_deberiaFallarSiDniDuplicado() {
        TrabajadorRequest request = requestBase();
        when(trabajadorRepository.findById(50L)).thenReturn(Optional.of(new Trabajador()));
        when(trabajadorRepository.existsByDniAndIdNot("12345678", 50L)).thenReturn(true);

        ApiException ex = assertThrows(ApiException.class, () -> trabajadorService.actualizar(50L, request));

        assertEquals(ErrorCode.BUSINESS_RULE_ERROR, ex.getErrorCode());
    }

    @Test
    void eliminar_deberiaInactivarTrabajadorYUsuario() {
        Usuario usuario = usuario(10L, "juan", "ACTIVO", 2);
        Trabajador trabajador = trabajador(1L, "ACTIVO", usuario, turno(20L, "Noche"), tipoJornada(30L, "Completa"));

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        trabajadorService.eliminar(1L);

        assertEquals("INACTIVO", trabajador.getEstado());
        assertEquals("INACTIVO", usuario.getEstado());
        verify(trabajadorRepository).save(trabajador);
        verify(usuarioRepository).save(usuario);
    }

    @Test
    void activar_deberiaActivarTrabajadorYUsuario() {
        Usuario usuario = usuario(10L, "juan", "INACTIVO", 2);
        Trabajador trabajador = trabajador(1L, "INACTIVO", usuario, turno(20L, "Noche"), tipoJornada(30L, "Completa"));

        when(trabajadorRepository.findById(1L)).thenReturn(Optional.of(trabajador));

        trabajadorService.activar(1L);

        assertEquals("ACTIVO", trabajador.getEstado());
        assertEquals("ACTIVO", usuario.getEstado());
        verify(trabajadorRepository).save(trabajador);
        verify(usuarioRepository, never()).save(any());
    }

    private TrabajadorRequest requestBase() {
        return new TrabajadorRequest(
                "Juan Carlos",
                "Ramirez",
                "12345678",
                "999888777",
                "juan@correo.com",
                LocalDate.now().minusMonths(1),
                LocalDate.now().plusMonths(6),
                "ACTIVO",
                10L,
                30L,
                20L
        );
    }

    private Usuario usuario(Long id, String username, String estado, Integer tipoUsuario) {
        Usuario u = new Usuario();
        u.setId(id);
        u.setUsername(username);
        u.setEstado(estado);
        u.setTipo_usuario(tipoUsuario);
        return u;
    }

    private Turno turno(Long id, String nombre) {
        Turno t = new Turno();
        t.setId(id);
        t.setNombre(nombre);
        return t;
    }

    private TipoJornada tipoJornada(Long id, String nombre) {
        TipoJornada tj = new TipoJornada();
        tj.setId(id);
        tj.setNombre(nombre);
        return tj;
    }

    private Trabajador trabajador(Long id, String estado, Usuario usuario, Turno turno, TipoJornada tipoJornada) {
        Trabajador t = new Trabajador();
        t.setId(id);
        t.setNombre("Juan Carlos");
        t.setApellido("Ramirez");
        t.setDni("12345678");
        t.setTelefono("999888777");
        t.setCorreo("juan@correo.com");
        t.setFecha_inicio(LocalDate.now().minusMonths(1));
        t.setFecha_fin(LocalDate.now().plusMonths(6));
        t.setEstado(estado);
        t.setUsuario(usuario);
        t.setTurno(turno);
        t.setTipoJornada(tipoJornada);
        return t;
    }
}