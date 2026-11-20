package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Simulação Moradia", description = "Operações de simulação moradia")
public class SimulacaoMoradiaController {

    private final SimulacaoMoradiaService service;

    public SimulacaoMoradiaController(SimulacaoMoradiaService service) {
        this.service = service;
    }

    @PostMapping("/planos-moradia/{planoId}/simulacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public SimulacaoMoradiaResponse cadastrar(@PathVariable Integer planoId, @RequestBody SimulacaoMoradiaRequest request, Authentication authentication) {
        return service.cadastrar(planoId, request, usuario(authentication));
    }

    @GetMapping("/planos-moradia/{planoId}/simulacoes")
    public List<SimulacaoMoradiaResponse> listarPorPlano(@PathVariable Integer planoId, Authentication authentication) {
        return service.listarPorPlano(planoId, usuario(authentication));
    }

    @GetMapping("/simulacoes-moradia/{id}")
    public SimulacaoMoradiaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/simulacoes-moradia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody SimulacaoMoradiaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/simulacoes-moradia/{id}")
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

    @GetMapping("/simulacoes-moradia/{id}/resumo")
    public ResumoSimulacaoMoradiaResponse resumo(@PathVariable Integer id, Authentication authentication) {
        return service.resumo(id, usuario(authentication));
    }
}
