package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recorrencia Tarefa", description = "Operações de recorrencia tarefa")
public class RecorrenciaTarefaController {

    private final RecorrenciaTarefaService service;

    public RecorrenciaTarefaController(RecorrenciaTarefaService service) {
        this.service = service;
    }

    @PostMapping("/tarefas/{tarefaId}/recorrencia")
    @ResponseStatus(HttpStatus.CREATED)
    public RecorrenciaTarefaResponse cadastrar(@PathVariable Integer tarefaId, @RequestBody RecorrenciaTarefaRequest request, Authentication authentication) {
        return service.cadastrar(tarefaId, request, usuario(authentication));
    }

    @GetMapping("/tarefas/{tarefaId}/recorrencia")
    public RecorrenciaTarefaResponse consultar(@PathVariable Integer tarefaId, Authentication authentication) {
        return service.consultar(tarefaId, usuario(authentication));
    }

    @PatchMapping("/tarefas/{tarefaId}/recorrencia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer tarefaId, @RequestBody RecorrenciaTarefaRequest request, Authentication authentication) {
        service.atualizar(tarefaId, request, usuario(authentication));
    }

    @DeleteMapping("/tarefas/{tarefaId}/recorrencia")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer tarefaId, Authentication authentication) {
        service.apagar(tarefaId, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
