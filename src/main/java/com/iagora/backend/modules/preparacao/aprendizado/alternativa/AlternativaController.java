package com.iagora.backend.modules.preparacao.aprendizado.alternativa;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Alternativa Quiz", description = "Catálogo de aprendizado.")
public class AlternativaController {
    private final AlternativaService service;

    private final AdministracaoService administracao;

    public AlternativaController(AlternativaService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @GetMapping("/questoes/{questaoId}/alternativas")
    public List<AlternativaResponse> listar(@PathVariable Integer questaoId) {
        return service.listar(questaoId);
    }

    @GetMapping("/alternativas/{id}")
    public AlternativaResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/questoes/{questaoId}/alternativas")
    @ResponseStatus(HttpStatus.CREATED)
    public AlternativaResponse cadastrar(@PathVariable Integer questaoId, @RequestBody AlternativaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(questaoId, request);
    }

    @PatchMapping("/alternativas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody AlternativaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @DeleteMapping("/alternativas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.apagar(id);
    }
}
