package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Execução Tarefa", description = "Operações de execução tarefa")
public class ExecucaoTarefaController {

    private final ExecucaoTarefaService service;

    public ExecucaoTarefaController(ExecucaoTarefaService service) {
        this.service = service;
    }

    @PostMapping("/tarefas/{tarefaId}/execucoes")
    @ResponseStatus(HttpStatus.CREATED)
    public ExecucaoTarefaResponse cadastrar(@PathVariable Integer tarefaId, @RequestBody ExecucaoTarefaRequest request, Authentication authentication) {
        return service.cadastrar(tarefaId, request, usuario(authentication));
    }

    @GetMapping("/tarefas/{tarefaId}/execucoes")
    public List<ExecucaoTarefaResponse> listarPorTarefa(@PathVariable Integer tarefaId, Authentication authentication) {
        return service.listarPorTarefa(tarefaId, usuario(authentication));
    }

    @GetMapping("/execucoes-tarefa/{id}")
    public ExecucaoTarefaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/execucoes-tarefa/usuario/{userId}")
    public List<ExecucaoTarefaResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/execucoes-tarefa/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody ExecucaoTarefaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/execucoes-tarefa/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/execucoes-tarefa/{id}/reabrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reabrir(@PathVariable Integer id, Authentication authentication) {
        service.reabrir(id, usuario(authentication));
    }

    @DeleteMapping("/execucoes-tarefa/{id}")
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
