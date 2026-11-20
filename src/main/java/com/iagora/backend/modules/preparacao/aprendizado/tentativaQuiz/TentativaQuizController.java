package com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "TentativaQuiz", description = "Acompanhamento do aprendizado.")
public class TentativaQuizController {
    private final TentativaQuizService service;

    public TentativaQuizController(TentativaQuizService service) {
        this.service = service;
    }

    @PostMapping("/quizzes/{quizId}/tentativas")
    @ResponseStatus(HttpStatus.CREATED)
    public TentativaQuizResponse cadastrar(@PathVariable Integer quizId, Authentication authentication) {
        return service.cadastrar(quizId, usuario(authentication));
    }

    @GetMapping("/quizzes/{quizId}/tentativas")
    public List<TentativaQuizResponse> listar(@PathVariable Integer quizId, Authentication authentication) {
        return service.listar(quizId, usuario(authentication));
    }

    @GetMapping("/tentativas-quiz/{id}")
    public TentativaQuizResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @PostMapping("/tentativas-quiz/{id}/finalizar")
    public TentativaQuizResultadoResponse finalizar(@PathVariable Integer id, Authentication authentication) {
        return service.finalizar(id, usuario(authentication));
    }

    @GetMapping("/tentativas-quiz/{id}/resultado")
    public TentativaQuizResultadoResponse resultado(@PathVariable Integer id, Authentication authentication) {
        return service.resultado(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
