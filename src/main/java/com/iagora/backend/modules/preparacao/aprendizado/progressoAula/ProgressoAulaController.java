package com.iagora.backend.modules.preparacao.aprendizado.progressoAula;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "ProgressoAula", description = "Acompanhamento do aprendizado.")
public class ProgressoAulaController {
    private final ProgressoAulaService service;

    public ProgressoAulaController(ProgressoAulaService service) {
        this.service = service;
    }

    @GetMapping("/progresso-aulas")
    public List<ProgressoAulaResponse> listar(Authentication authentication) {
        return service.listar(usuario(authentication));
    }

    @GetMapping("/progresso-aulas/resumo")
    public ProgressoAulaResumoResponse resumo(Authentication authentication) {
        return service.resumo(usuario(authentication));
    }

    @GetMapping("/aulas/{aulaId}/progresso")
    public ProgressoAulaResponse consultar(@PathVariable Integer aulaId, Authentication authentication) {
        return service.consultar(aulaId, usuario(authentication));
    }

    @PatchMapping("/aulas/{aulaId}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer aulaId, Authentication authentication) {
        service.concluir(aulaId, usuario(authentication));
    }

    @PatchMapping("/aulas/{aulaId}/reabrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reabrir(@PathVariable Integer aulaId, Authentication authentication) {
        service.reabrir(aulaId, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
