package com.iagora.backend.modules.marketplace.publicacaoServico;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PublicacaoServico", description = "Operações de PublicacaoServico.")
public class PublicacaoServicoController {
    private final PublicacaoServicoService service;

    public PublicacaoServicoController(PublicacaoServicoService service) {
        this.service = service;
    }

    @PostMapping("/servicos/{servicoId}/publicacoes")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicacaoServicoResponse cadastrar(@PathVariable Integer servicoId, @RequestBody PublicacaoServicoRequest request, Authentication authentication) {
        return service.cadastrar(servicoId, request, usuario(authentication));
    }

    @GetMapping("/servicos/{servicoId}/publicacoes")
    public List<PublicacaoServicoResponse> listar(@PathVariable Integer servicoId, Authentication authentication) {
        return service.listar(servicoId, usuario(authentication));
    }

    @GetMapping("/publicacoes-servico/{id}")
    public PublicacaoServicoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @GetMapping("/empresas/minhas-publicacoes")
    public List<PublicacaoServicoResponse> minhas(Authentication authentication) {
        return service.minhas(usuario(authentication));
    }

    @PostMapping("/publicacoes-servico/{id}/renovar")
    @ResponseStatus(HttpStatus.CREATED)
    public PublicacaoServicoResponse renovar(@PathVariable Integer id, Authentication authentication) {
        return service.renovar(id, usuario(authentication));
    }

    @PatchMapping("/publicacoes-servico/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Integer id, Authentication authentication) {
        service.cancelar(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
