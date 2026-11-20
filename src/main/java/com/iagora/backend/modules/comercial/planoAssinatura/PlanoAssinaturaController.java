package com.iagora.backend.modules.comercial.planoAssinatura;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PlanoAssinatura", description = "Operações de PlanoAssinatura.")
public class PlanoAssinaturaController {
    private final PlanoAssinaturaService service;
    private final AdministracaoService administracao;

    public PlanoAssinaturaController(PlanoAssinaturaService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/planos-assinatura")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoAssinaturaResponse cadastrar(@RequestBody PlanoAssinaturaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(request);
    }

    @GetMapping("/planos-assinatura")
    public List<PlanoAssinaturaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/planos-assinatura/{id}")
    public PlanoAssinaturaResponse detalhar(@PathVariable Integer id) {
        return service.detalhar(id);
    }

    @PatchMapping("/planos-assinatura/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody PlanoAssinaturaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @PatchMapping("/planos-assinatura/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.ativar(id);
    }

    @PatchMapping("/planos-assinatura/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.desativar(id);
    }
}
