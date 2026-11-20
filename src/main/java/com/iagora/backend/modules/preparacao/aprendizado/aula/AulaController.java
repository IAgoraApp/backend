package com.iagora.backend.modules.preparacao.aprendizado.aula;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Aulas", description = "Catálogo de aprendizado.")
public class AulaController {
    private final AulaService service;

    private final AdministracaoService administracao;

    public AulaController(AulaService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @GetMapping("/aulas")
    public List<AulaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/aulas/{id}")
    public AulaResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/aulas")
    @ResponseStatus(HttpStatus.CREATED)
    public AulaResponse cadastrar(@RequestBody AulaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(request);
    }

    @PatchMapping("/aulas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody AulaRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @DeleteMapping("/aulas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.apagar(id);
    }
}
