package com.iagora.backend.modules.casa.tarefa.categoriaTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Categoria Tarefa", description = "Operações de categoria tarefa")
public class CategoriaTarefaController {

    private final CategoriaTarefaService service;

    public CategoriaTarefaController(CategoriaTarefaService service) {
        this.service = service;
    }

    @PostMapping("/categorias-tarefa")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaTarefaResponse cadastrar(@RequestBody CategoriaTarefaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/categorias-tarefa/{id}")
    public CategoriaTarefaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/categorias-tarefa/usuario/{userId}")
    public List<CategoriaTarefaResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/categorias-tarefa/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody CategoriaTarefaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/categorias-tarefa/{id}")
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
