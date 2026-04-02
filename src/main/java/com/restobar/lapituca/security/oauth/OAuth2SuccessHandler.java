package com.restobar.lapituca.security.oauth;

import com.restobar.lapituca.dto.auth.LoginResponse;
import com.restobar.lapituca.exception.ApiException;
import com.restobar.lapituca.service.AuthService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Value("${app.frontend.oauth-success-url:http://localhost:5173/auth/google/success}")
    private String frontendSuccessUrl;

    @Value("${app.frontend.login-url:http://localhost:5173/login}")
    private String frontendLoginUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {

        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String email = oauth2User.getAttribute("email");
        String name = oauth2User.getAttribute("name");
        String picture = oauth2User.getAttribute("picture");

        try {
            LoginResponse loginResponse = authService.procesarLoginGoogle(email, name, picture);

            String redirectUrl = UriComponentsBuilder.fromUriString(frontendSuccessUrl)
                    .queryParam("token", loginResponse.getToken())
                    .queryParam("usuarioId", loginResponse.getUsuarioId())
                    .queryParam("correo", loginResponse.getCorreo())
                    .queryParam("rol", loginResponse.getRol())
                    .queryParam("proveedor", loginResponse.getProveedor())
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUriString();

            response.sendRedirect(redirectUrl);

        } catch (ApiException ex) {
            String redirectError = UriComponentsBuilder.fromUriString(frontendLoginUrl)
                    .queryParam("oauth_error", ex.getMessage())
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectError);
        } catch (Exception ex) {
            String redirectError = UriComponentsBuilder.fromUriString(frontendLoginUrl)
                    .queryParam("oauth_error", "No se pudo autenticar con Google")
                    .encode(StandardCharsets.UTF_8)
                    .build()
                    .toUriString();
            response.sendRedirect(redirectError);
        }
    }
}