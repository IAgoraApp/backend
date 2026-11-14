package com.iagora.backend.security;

import java.nio.charset.StandardCharsets;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

  @Value("${security.jwt.enabled:true}")
  private boolean jwtEnabled;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http)
    throws Exception {
    http.csrf(csrf -> csrf.disable());

    http.authorizeHttpRequests(auth -> {
      if (jwtEnabled) {
        auth
          .requestMatchers(
            "/user/register",
            "/user/login",
            "/api/docs",
            "/api/health",
            "/api/docs/**",
            "/api/swagger-ui/**",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/user/forgot-password",
            "/user/reset-password"
          )
          .permitAll()
          .anyRequest()
          .authenticated();
      } else {
        auth.anyRequest().permitAll();
      }
    });

    http.sessionManagement(session ->
      session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
    );

    http.httpBasic(basic -> basic.disable());
    http.formLogin(form -> form.disable());

    if (jwtEnabled) {
      http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}));
    }

    http.exceptionHandling(exception ->
      exception
        .authenticationEntryPoint((request, response, authException) -> {
          response.setStatus(HttpStatus.UNAUTHORIZED.value());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.setCharacterEncoding(StandardCharsets.UTF_8.name());

          response
            .getWriter()
            .write(
              "{\"mensagem\":\"Acesso não autorizado, faça login para continuar\"}"
            );
        })
        .accessDeniedHandler((request, response, accessDeniedException) -> {
          response.setStatus(HttpStatus.FORBIDDEN.value());
          response.setContentType(MediaType.APPLICATION_JSON_VALUE);
          response.setCharacterEncoding(StandardCharsets.UTF_8.name());

          response
            .getWriter()
            .write(
              "{\"mensagem\":\"Você não tem permissão para acessar este recurso\"}"
            );
        })
    );

    return http.build();
  }
}
