package com.restobar.lapituca.controller;

import com.restobar.lapituca.config.SecurityConfig;
import com.restobar.lapituca.dto.auth.AuthMeResponse;
import com.restobar.lapituca.dto.auth.ForgotPasswordResponse;
import com.restobar.lapituca.dto.auth.LoginResponse;
import com.restobar.lapituca.dto.auth.PasswordResetResponse;
import com.restobar.lapituca.dto.auth.RegisterResponse;
import com.restobar.lapituca.dto.auth.VerifyResetTokenResponse;
import com.restobar.lapituca.dto.response.ClienteResponse;
import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import com.restobar.lapituca.service.AuthService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private CustomOAuth2UserService customOAuth2UserService;

    @MockBean
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    @MockBean
    private OAuth2FailureHandler oAuth2FailureHandler;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp() throws Exception {
        doAnswer(invocation -> {
            ServletRequest request = invocation.getArgument(0);
            ServletResponse response = invocation.getArgument(1);
            FilterChain filterChain = invocation.getArgument(2);
            filterChain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
    }

    @Test
    void login_deberiaRetornar200() throws Exception {
        LoginResponse response = new LoginResponse("jwt-token", 10L, "user@mail.com", "CLIENTE", "LOCAL");
        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "user@mail.com",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"))
                .andExpect(jsonPath("$.correo").value("user@mail.com"));
    }

    @Test
    void login_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "correo-invalido",
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).login(any());
    }

    @Test
    void register_deberiaRetornar200() throws Exception {
        RegisterResponse response = new RegisterResponse(
                "jwt-token",
                20L,
                "nuevo@mail.com",
                "CLIENTE",
                "LOCAL",
                new ClienteResponse(
                        30L,
                        "Juan",
                        "Perez",
                        LocalDate.of(2000, 1, 1),
                        "999888777",
                        "nuevo@mail.com",
                        "ACTIVO",
                        "REGULAR",
                        "Centro",
                        null,
                        null,
                        20L,
                        "user@mail.com"
                )
        );
        when(authService.registerClienteLocal(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "Juan",
                                  "apellido": "Perez",
                                  "fechaNacimiento": "2000-01-01",
                                  "correo": "nuevo@mail.com",
                                  "telefono": "999888777",
                                  "distrito": "Centro",
                                  "password": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("nuevo@mail.com"));
    }

    @Test
    void register_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nombre": "",
                                  "apellido": "P",
                                  "fechaNacimiento": null,
                                  "correo": "correo-invalido",
                                  "telefono": "123",
                                  "distrito": "",
                                  "password": "123"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).registerClienteLocal(any());
    }

    @Test
    void forgotPassword_deberiaRetornar200() throws Exception {
        when(authService.requestPasswordReset(any())).thenReturn(new ForgotPasswordResponse("Código enviado"));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "user@mail.com"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Código enviado"));
    }


    @Test
    void forgotPassword_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).requestPasswordReset(any());
    }

    @Test
    void verifyResetToken_deberiaRetornar200() throws Exception {
        when(authService.verifyResetToken(any())).thenReturn(new VerifyResetTokenResponse("Válido", "user@mail.com", "2026-01-01T10:00:00"));

        mockMvc.perform(post("/api/auth/verify-reset-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "user@mail.com",
                                  "token": "123456"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("user@mail.com"));
    }


    @Test
    void verifyResetToken_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/verify-reset-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "correo-invalido",
                                  "token": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).verifyResetToken(any());
    }

    @Test
    void resetPassword_deberiaRetornar200() throws Exception {
        when(authService.resetPassword(any())).thenReturn(new PasswordResetResponse("Contraseña actualizada"));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "user@mail.com",
                                  "token": "123456",
                                  "password": "Password@123",
                                  "confirmPassword": "Password@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Contraseña actualizada"));
    }


    @Test
    void resetPassword_deberiaRetornar400_cuandoRequestInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "correo": "correo-invalido",
                                  "token": "",
                                  "password": "123",
                                  "confirmPassword": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(authService, never()).resetPassword(any());
    }

    @Test
    @WithMockUser(username = "user@mail.com", roles = "CLIENTE")
    void me_deberiaRetornar200_cuandoAutenticado() throws Exception {
        AuthMeResponse response = new AuthMeResponse(1L, "user@mail.com", "CLIENTE", "LOCAL", "ACTIVO", 22L, "Juan Perez", null);
        when(authService.getAuthenticatedUser("user@mail.com")).thenReturn(response);

        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("user@mail.com"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"));
    }

    @Test
    void me_deberiaRetornar401_cuandoNoAutenticado() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());

        verify(authService, never()).getAuthenticatedUser(any());
    }
}
