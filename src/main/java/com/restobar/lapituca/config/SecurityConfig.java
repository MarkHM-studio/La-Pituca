package com.restobar.lapituca.config;

import com.restobar.lapituca.security.jwt.JwtAuthenticationFilter;
import com.restobar.lapituca.security.oauth.CustomOAuth2UserService;
import com.restobar.lapituca.security.oauth.OAuth2FailureHandler;
import com.restobar.lapituca.security.oauth.OAuth2SuccessHandler;
import com.restobar.lapituca.security.service.CustomUserDetailsService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.frontend.allowed-origin:http://localhost:5173}")
    private String frontendAllowedOrigin;

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            CustomOAuth2UserService customOAuth2UserService,
            OAuth2SuccessHandler oAuth2SuccessHandler,
            OAuth2FailureHandler oAuth2FailureHandler,
            DaoAuthenticationProvider authenticationProvider
    ) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No autorizado"))
                )
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        // PERMITIR PREFLIGHT
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        //PÚBLICOS
                        .requestMatchers(
                                "/api/auth/login",
                                "/api/auth/register",
                                "/api/auth/forgot-password",
                                "/api/auth/verify-reset-token",
                                "/api/auth/reset-password",
                                "/oauth2/**",
                                "/login/**",
                                "/h2-console/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/distrito/**").permitAll() //Incluye listarTodos(), obtenerPorId()
                        .requestMatchers("/api/mercadopago/webhook").permitAll()

                        //PROTEGIDOS
                        .requestMatchers("/transacciones/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(user -> user.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                        .failureHandler(oAuth2FailureHandler)
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of(
                "http://localhost:5173",
                "https://restobar-lapituca.vercel.app",
                "https://*.vercel.app",
                "https://restobar-lapituca.vercel.app?_vercel_share=1Mxyh7B7gZFNaODp8b4tn2Nv3xd0RiQI"
        ));

        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*")); // <- importante para ngrok-skip-browser-warning
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder
    ) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}