package com.iagora.backend.modules.preparacao.aprendizado.respostaQuiz;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "RespostaQuiz", description = "Acompanhamento do aprendizado.")
public class RespostaQuizController {
    private final RespostaQuizService service;

    public RespostaQuizController(RespostaQuizService service) {
        this.service = service;
    }

    @PostMapping("/tentativas-quiz/{tentativaId}/respostas")
    @ResponseStatus(HttpStatus.CREATED)
    public RespostaQuizResponse cadastrar(@PathVariable Integer tentativaId, @RequestBody RespostaQuizRequest request, Authentication authentication) {
        return service.cadastrar(tentativaId, request, usuario(authentication));
    }

    @GetMapping("/tentativas-quiz/{tentativaId}/respostas")
    public List<RespostaQuizResponse> listar(@PathVariable Integer tentativaId, Authentication authentication) {
        return service.listar(tentativaId, usuario(authentication));
    }

    @PatchMapping("/respostas-quiz/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody RespostaQuizRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/respostas-quiz/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        service.apagar(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
