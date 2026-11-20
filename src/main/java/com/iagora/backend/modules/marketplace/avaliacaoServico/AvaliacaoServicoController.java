package com.iagora.backend.modules.marketplace.avaliacaoServico;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "AvaliacaoServico", description = "Operações de AvaliacaoServico.")
public class AvaliacaoServicoController {
    private final AvaliacaoServicoService service;

    public AvaliacaoServicoController(AvaliacaoServicoService service) {
        this.service = service;
    }

    @PostMapping("/contratacoes/{contratacaoId}/avaliacao")
    @ResponseStatus(HttpStatus.CREATED)
    public AvaliacaoServicoResponse cadastrar(@PathVariable Integer contratacaoId, @RequestBody AvaliacaoServicoRequest request, Authentication authentication) {
        return service.cadastrar(contratacaoId, request, usuario(authentication));
    }

    @GetMapping("/servicos/{servicoId}/avaliacoes")
    public List<AvaliacaoServicoResponse> listar(@PathVariable Integer servicoId) {
        return service.listar(servicoId);
    }

    @GetMapping("/avaliacoes/{id}")
    public AvaliacaoServicoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @GetMapping("/minhas-avaliacoes")
    public List<AvaliacaoServicoResponse> minhas(Authentication authentication) {
        return service.minhas(usuario(authentication));
    }

    @PatchMapping("/avaliacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody AvaliacaoServicoRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/avaliacoes/{id}")
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
