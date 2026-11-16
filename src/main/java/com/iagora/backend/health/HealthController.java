
package com.iagora.backend.health;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Sistema - Saúde", description = "Verificação da API e da conexão com o banco de dados.")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    private final JdbcTemplate jdbcTemplate;

    public HealthController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public Map<String, String> health() {

        jdbcTemplate.queryForObject("SELECT 1", Integer.class);

        return Map.of(
            "api", "online",
            "database", "connected"
        );
    }
}
