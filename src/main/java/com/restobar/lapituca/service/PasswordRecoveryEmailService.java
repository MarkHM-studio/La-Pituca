package com.restobar.lapituca.service;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordRecoveryEmailService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final JavaMailSender mailSender;

    @Setter
    @Value("${app.mail.enabled:false}")
    private boolean mailEnabled;

    @Setter
    @Value("${app.mail.from:no-reply@lapituca.com}")
    private String mailFrom;

    public void sendPasswordRecoveryEmail(String correoDestino, String nombreDestino, String token, String resetLink, LocalDateTime expiresAt) {
        String asunto = "Recuperación de contraseña - La Pituca";
        String cuerpo = buildBody(nombreDestino, token, resetLink, expiresAt);

        if (!mailEnabled) {
            log.info("[MAIL-DESACTIVADO] Recuperación de contraseña para {}. Código: {}. Link: {}", correoDestino, token, resetLink);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(correoDestino);
        message.setSubject(asunto);
        message.setText(cuerpo);
        mailSender.send(message);
    }

    private String buildBody(String nombreDestino, String token, String resetLink, LocalDateTime expiresAt) {
        return String.format(
                "Hola %s,%n%n" +
                        "Recibimos una solicitud para restablecer tu contraseña en La Pituca.%n%n" +
                        "Código de recuperación: %s%n" +
                        "Enlace directo: %s%n" +
                        "Válido hasta: %s%n%n" +
                        "Si no solicitaste este cambio, ignora este correo.%n",
                nombreDestino,
                token,
                resetLink,
                expiresAt.format(DATE_TIME_FORMATTER)
        );
    }
}