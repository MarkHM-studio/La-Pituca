package com.restobar.lapituca.security.oauth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    @Value("${app.frontend.login-url:http://localhost:5173/login}")
    private String frontendLoginUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException {
        String message = exception.getMessage() != null ? exception.getMessage() : "No se pudo autenticar con Google";

        String redirectUrl = UriComponentsBuilder.fromUriString(frontendLoginUrl)
                .queryParam("oauth_error", message)
                .build(true)
                .toUriString();

        response.sendRedirect(redirectUrl);
    }
}