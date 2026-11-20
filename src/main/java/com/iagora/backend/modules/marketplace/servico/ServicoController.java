package com.iagora.backend.modules.marketplace.servico;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Servico", description = "Operações de Servico.")
public class ServicoController {
    private final ServicoService service;
    private final AdministracaoService administracao;

    public ServicoController(ServicoService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/servicos")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoResponse cadastrar(@RequestBody ServicoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/servicos")
    public List<ServicoResponse> listar() {
        return service.listar();
    }

    @GetMapping("/servicos/{id}")
    public ServicoResponse detalhar(@PathVariable Integer id, Authentication authentication) {
        return service.detalhar(id, usuario(authentication));
    }

    @GetMapping("/empresas/meus-servicos")
    public List<ServicoResponse> meusServicos(Authentication authentication) {
        return service.meusServicos(usuario(authentication));
    }

    @PatchMapping("/servicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody ServicoRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/servicos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        service.apagar(id, usuario(authentication));
    }

    @PostMapping("/servicos/{id}/enviar-validacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarValidacao(@PathVariable Integer id, Authentication authentication) {
        service.enviarValidacao(id, usuario(authentication));
    }

    @PatchMapping("/servicos/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Integer id, Authentication authentication) {
        service.ativar(id, usuario(authentication));
    }

    @PatchMapping("/servicos/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id, Authentication authentication) {
        service.desativar(id, usuario(authentication));
    }

    @GetMapping("/admin/servicos/pendentes")
    public List<ServicoResponse> pendentes(Authentication authentication) {
        administracao.verificar(authentication);
        return service.pendentes();
    }

    @PatchMapping("/admin/servicos/{id}/aprovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aprovar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.aprovar(id);
    }

    @PatchMapping("/admin/servicos/{id}/reprovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reprovar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.reprovar(id);
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
