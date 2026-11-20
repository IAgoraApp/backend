package com.iagora.backend.modules.preparacao.dinheiroPreparacao;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import tools.jackson.databind.JsonNode;

@RestController
@RequestMapping("/meu-dinheiro")
@Tag(name = "Meu dinheiro", description = "Renda, reserva e previsão de mudança.")
public class DinheiroPreparacaoController {

    private final DinheiroPreparacaoService service;

    public DinheiroPreparacaoController(DinheiroPreparacaoService service) {
        this.service = service;
    }

    @GetMapping
    public DinheiroPreparacaoResponse consultar(Authentication authentication) {
        return service.consultar(usuario(authentication));
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@RequestBody JsonNode body, Authentication authentication) {
        if (!body.isObject()) {
            throw new RegraNegocioException("Informe um objeto JSON", HttpStatus.BAD_REQUEST);
        }
        service.atualizar(usuario(authentication),
            new DinheiroPreparacaoRequest(body.get("renda"), body.get("reserva"), body.get("mudancaPrevista")));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
