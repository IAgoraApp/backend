package com.iagora.backend.modules.preparacao.moradia.simulacaoItem;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Item Simulação", description = "Operações de item simulação")
public class SimulacaoItemController {

    private final SimulacaoItemService service;

    public SimulacaoItemController(SimulacaoItemService service) {
        this.service = service;
    }

    @PostMapping("/simulacoes-moradia/{simulacaoId}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    public SimulacaoItemResponse cadastrar(@PathVariable Integer simulacaoId, @RequestBody SimulacaoItemRequest request, Authentication authentication) {
        return service.cadastrar(simulacaoId, request, usuario(authentication));
    }

    @GetMapping("/simulacoes-moradia/{simulacaoId}/itens")
    public List<SimulacaoItemResponse> listarPorSimulacao(@PathVariable Integer simulacaoId, Authentication authentication) {
        return service.listarPorSimulacao(simulacaoId, usuario(authentication));
    }

    @GetMapping("/simulacoes-itens/{id}")
    public SimulacaoItemResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/simulacoes-itens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody SimulacaoItemRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/simulacoes-itens/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        service.apagar(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
