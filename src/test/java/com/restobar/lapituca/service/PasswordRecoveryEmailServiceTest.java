package com.restobar.lapituca.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Field;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordRecoveryEmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PasswordRecoveryEmailService service;

    private final LocalDateTime fecha = LocalDateTime.of(2026, 4, 12, 18, 0);

    @BeforeEach
    void setup() {
        service.setMailEnabled(true);
        service.setMailFrom("test@lapituca.com");
    }

    @Test
    void sendPasswordRecoveryEmail_deberiaEnviarCorreo_cuandoMailHabilitado() {
        service.sendPasswordRecoveryEmail(
                "destino@test.com",
                "Mark",
                "123456",
                "http://reset-link",
                fecha
        );

        var captor = forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage message = captor.getValue();


        assertEquals("test@lapituca.com", message.getFrom());
        assertNotNull(message.getTo());
        assertEquals("destino@test.com", message.getTo()[0]);
        assertEquals("Recuperación de contraseña - La Pituca", message.getSubject());

        String body = message.getText();
        assertNotNull(body);

        assertTrue(body.contains("Mark"));
        assertTrue(body.contains("123456"));
        assertTrue(body.contains("http://reset-link"));
        assertTrue(body.contains("12/04/2026 18:00"));
    }

    @Test
    void sendPasswordRecoveryEmail_noDeberiaEnviarCorreo_cuandoMailDeshabilitado() {
        service.setMailEnabled(false);

        service.sendPasswordRecoveryEmail(
                "destino@test.com",
                "Mark",
                "123456",
                "http://reset-link",
                fecha
        );

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendPasswordRecoveryEmail_deberiaConstruirBodyCorrectamente() {
        service.sendPasswordRecoveryEmail(
                "destino@test.com",
                "Juan",
                "ABC123",
                "http://reset-link",
                fecha
        );

        var captor = forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String body = captor.getValue().getText();
        assertNotNull(body);

        assertTrue(body.contains("Hola Juan"));
        assertTrue(body.contains("Código de recuperación: ABC123"));
        assertTrue(body.contains("Enlace directo: http://reset-link"));
        assertTrue(body.contains("Válido hasta: 12/04/2026 18:00"));
    }

    // 🔧 helper para setear @Value
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}