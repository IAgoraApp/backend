package com.iagora.backend.security;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.iagora.backend.config.CorsConfig;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

@SpringJUnitWebConfig(CorsSecurityTests.TestConfig.class)
@TestPropertySource(properties = {
  "security.jwt.enabled=true",
  "jwt.secret=MDEyMzQ1Njc4OWFiY2RlZjAxMjM0NTY3ODlhYmNkZWY=",
  "jwt.expiration=60000"
})
class CorsSecurityTests {

  private static final String ORIGIN = "https://iagoraweb.com.br";

  @Autowired
  private WebApplicationContext context;

  @Autowired
  @Qualifier("springSecurityFilterChain")
  private Filter securityFilterChain;

  @Autowired
  private JwtService jwtService;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.webAppContextSetup(context)
      .addFilters(securityFilterChain)
      .build();
  }

  @ParameterizedTest
  @ValueSource(strings = { "https://iagoraweb.com.br", "https://www.iagoraweb.com.br" })
  void allowsPreflightWithoutJwtOnProtectedRoutes(String origin) throws Exception {
    for (String method : new String[] { "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" }) {
      mockMvc.perform(options("/user/1")
          .header(HttpHeaders.ORIGIN, origin)
          .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, method)
          .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "Authorization, Content-Type, Accept"))
        .andExpect(status().isOk())
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, containsString(method)))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Authorization")))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Content-Type")))
        .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, containsString("Accept")));
    }
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "https://example.com", "http://iagoraweb.com.br", "https://preview.vercel.app",
    "https://iagoraweb.com.br.example.com", "https://sub.iagoraweb.com.br", "null"
  })
  void rejectsUnapprovedOrigins(String origin) throws Exception {
    mockMvc.perform(options("/user/1")
        .header(HttpHeaders.ORIGIN, origin)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET"))
      .andExpect(status().isForbidden())
      .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

    mockMvc.perform(get("/user/1")
        .header(HttpHeaders.ORIGIN, origin)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(1)))
      .andExpect(status().isForbidden())
      .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void rejectsUnapprovedMethodsAndHeaders() throws Exception {
    mockMvc.perform(options("/user/1")
        .header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "TRACE"))
      .andExpect(status().isForbidden())
      .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));

    mockMvc.perform(options("/user/1")
        .header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "X-Unapproved-Header"))
      .andExpect(status().isForbidden())
      .andExpect(header().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN));
  }

  @Test
  void keepsProtectedRoutesUnauthorizedWithoutJwt() throws Exception {
    mockMvc.perform(get("/user/1").header(HttpHeaders.ORIGIN, ORIGIN))
      .andExpect(status().isUnauthorized())
      .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
      .andExpect(content().json("{\"mensagem\":\"Acesso não autorizado, faça login para continuar\"}"));

    mockMvc.perform(get("/user/1"))
      .andExpect(status().isUnauthorized());

    mockMvc.perform(options("/user/1").header(HttpHeaders.ORIGIN, ORIGIN))
      .andExpect(status().isUnauthorized());
  }

  @Test
  void acceptsValidJwtWithoutCreatingSession() throws Exception {
    mockMvc.perform(get("/user/1")
        .header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwtService.gerarToken(1)))
      .andExpect(status().isOk())
      .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
      .andExpect(result -> assertNull(result.getRequest().getSession(false)))
      .andExpect(content().string("private"));
  }

  @Test
  void rejectsInvalidJwtWithCorsHeaders() throws Exception {
    mockMvc.perform(get("/user/1")
        .header(HttpHeaders.ORIGIN, ORIGIN)
        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
      .andExpect(status().isUnauthorized())
      .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
  }

  @Test
  void keepsPublicPostAccessibleWithoutJwtOrCsrfToken() throws Exception {
    mockMvc.perform(post("/user/login").header(HttpHeaders.ORIGIN, ORIGIN))
      .andExpect(status().isOk())
      .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN));
  }

  @Configuration
  @EnableWebMvc
  @EnableWebSecurity
  @Import({ SecurityConfig.class, CorsConfig.class, JwtService.class })
  static class TestConfig {

    @Bean
    TestController testController() {
      return new TestController();
    }
  }

  @RestController
  static class TestController {

    @GetMapping("/user/1")
    String privateEndpoint() {
      return "private";
    }

    @PostMapping("/user/login")
    String publicEndpoint() {
      return "public";
    }
  }
}
