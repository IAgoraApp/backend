package com.iagora.backend.modules.preparacao.aprendizado.questao;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Questão Quiz", description = "Catálogo de aprendizado.")
public class QuestaoController {
    private final QuestaoService service;

    private final AdministracaoService administracao;

    public QuestaoController(QuestaoService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @GetMapping("/quizzes/{quizId}/questoes")
    public List<QuestaoResponse> listar(@PathVariable Integer quizId) {
        return service.listar(quizId);
    }

    @GetMapping("/questoes/{id}")
    public QuestaoResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/quizzes/{quizId}/questoes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuestaoResponse cadastrar(@PathVariable Integer quizId, @RequestBody QuestaoRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(quizId, request);
    }

    @PatchMapping("/questoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody QuestaoRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @DeleteMapping("/questoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.apagar(id);
    }
}
