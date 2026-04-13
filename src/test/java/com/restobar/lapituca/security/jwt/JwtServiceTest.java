package com.restobar.lapituca.security.jwt;

import com.restobar.lapituca.entity.Rol;
import com.restobar.lapituca.entity.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Test
    void generateAndValidateToken_ok() {
        JwtService jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "jwtSecret", "U3VwZXJTZWNyZXRvUGFyYVBpdHVjYTAxMjM0NTY3ODkwMTIzNDU2Nzg5MDEyMzQ1Njc4OTA=");
        ReflectionTestUtils.setField(jwtService, "jwtExpirationMs", 60_000L);

        Rol rol = new Rol();
        rol.setNombre("CLIENTE");

        Usuario usuario = new Usuario();
        usuario.setId(12L);
        usuario.setUsername("cliente@test.com");
        usuario.setRol(rol);

        String token = jwtService.generateToken(usuario);

        assertEquals("cliente@test.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, "cliente@test.com"));
        assertFalse(jwtService.isTokenValid(token, "otro@test.com"));
    }
}