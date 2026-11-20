package com.iagora.backend.modules.preparacao.aprendizado.quiz;

import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Quiz", description = "Catálogo de aprendizado.")
public class QuizController {
    private final QuizService service;

    private final AdministracaoService administracao;

    public QuizController(QuizService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @GetMapping("/aulas/{aulaId}/quizzes")
    public List<QuizResponse> listar(@PathVariable Integer aulaId) {
        return service.listar(aulaId);
    }

    @GetMapping("/quizzes/{id}")
    public QuizResponse buscarPorId(@PathVariable Integer id) {
        return service.buscarPorId(id);
    }

    @PostMapping("/aulas/{aulaId}/quizzes")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizResponse cadastrar(@PathVariable Integer aulaId, @RequestBody QuizRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        return service.cadastrar(aulaId, request);
    }

    @PatchMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody QuizRequest request, Authentication authentication) {
        administracao.verificar(authentication);
        service.atualizar(id, request);
    }

    @DeleteMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.apagar(id);
    }
}
