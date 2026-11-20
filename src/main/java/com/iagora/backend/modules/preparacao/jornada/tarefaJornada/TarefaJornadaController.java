package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Tarefa Jornada", description = "Operações de tarefa jornada")
public class TarefaJornadaController {

    private final TarefaJornadaService service;

    public TarefaJornadaController(TarefaJornadaService service) {
        this.service = service;
    }

    @PostMapping("/etapas-jornada/{etapaId}/tarefas")
    @ResponseStatus(HttpStatus.CREATED)
    public TarefaJornadaResponse cadastrar(@PathVariable Integer etapaId, @RequestBody TarefaJornadaRequest request, Authentication authentication) {
        return service.cadastrar(etapaId, request, usuario(authentication));
    }

    @GetMapping("/etapas-jornada/{etapaId}/tarefas")
    public List<TarefaJornadaResponse> listarPorEtapa(@PathVariable Integer etapaId, Authentication authentication) {
        return service.listarPorEtapa(etapaId, usuario(authentication));
    }

    @GetMapping("/tarefas-jornada/{id}")
    public TarefaJornadaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/tarefas-jornada/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody TarefaJornadaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/tarefas-jornada/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/tarefas-jornada/{id}/reabrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reabrir(@PathVariable Integer id, Authentication authentication) {
        service.reabrir(id, usuario(authentication));
    }

    @DeleteMapping("/tarefas-jornada/{id}")
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

    @PatchMapping("/etapas-jornada/{etapaId}/tarefas/ordenar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ordenar(@PathVariable Integer etapaId, @RequestBody OrdenarTarefaJornadaRequest request, Authentication authentication) {
        service.ordenar(etapaId, request, usuario(authentication));
    }
}
