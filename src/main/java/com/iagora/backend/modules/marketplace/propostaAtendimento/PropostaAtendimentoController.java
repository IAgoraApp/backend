package com.iagora.backend.modules.marketplace.propostaAtendimento;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PropostaAtendimento", description = "Operações de PropostaAtendimento.")
public class PropostaAtendimentoController {
    private final PropostaAtendimentoService service;

    public PropostaAtendimentoController(PropostaAtendimentoService service) {
        this.service = service;
    }

    @GetMapping("/contratacoes/{id}/propostas")
    public List<PropostaAtendimentoResponse> listar(@PathVariable Integer id, Authentication authentication) {
        return service.listar(id, usuario(authentication));
    }

    @PostMapping("/contratacoes/{id}/propostas")
    @ResponseStatus(HttpStatus.CREATED)
    public PropostaAtendimentoResponse cadastrar(@PathVariable Integer id, @RequestBody PropostaAtendimentoRequest request, Authentication authentication) {
        return service.cadastrar(id, request, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/propostas/{propostaId}/confirmar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmar(@PathVariable Integer id, @PathVariable Integer propostaId, Authentication authentication) {
        service.confirmar(id, propostaId, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/propostas/{propostaId}/recusar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recusar(@PathVariable Integer id, @PathVariable Integer propostaId, Authentication authentication) {
        service.recusar(id, propostaId, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/propostas/{propostaId}/retirar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void retirar(@PathVariable Integer id, @PathVariable Integer propostaId, Authentication authentication) {
        service.retirar(id, propostaId, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
