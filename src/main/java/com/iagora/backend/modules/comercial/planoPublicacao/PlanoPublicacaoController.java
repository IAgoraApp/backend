package com.iagora.backend.modules.comercial.planoPublicacao;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PlanoPublicacao", description = "Operações de PlanoPublicacao.")
public class PlanoPublicacaoController {
    private final PlanoPublicacaoService service;
    private final AdministracaoService administracao;

    public PlanoPublicacaoController(PlanoPublicacaoService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/planos-publicacao")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoPublicacaoResponse cadastrar(@RequestBody PlanoPublicacaoRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(request);
    }

    @GetMapping("/planos-publicacao")
    public List<PlanoPublicacaoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/planos-publicacao/{id}")
    public PlanoPublicacaoResponse detalhar(@PathVariable Integer id) {
        return service.detalhar(id);
    }

    @PatchMapping("/planos-publicacao/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody PlanoPublicacaoRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @PatchMapping("/planos-publicacao/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.ativar(id);
    }

    @PatchMapping("/planos-publicacao/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.desativar(id);
    }
}
