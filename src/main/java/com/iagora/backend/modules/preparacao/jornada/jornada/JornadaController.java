package com.iagora.backend.modules.preparacao.jornada.jornada;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaService;
import com.iagora.backend.modules.preparacao.jornada.tarefaJornada.TarefaJornadaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Jornada", description = "Operações de jornada")
public class JornadaController {

    private final JornadaService service;

    private final EtapaJornadaService etapas;
    private final TarefaJornadaService tarefas;
    private final EntityManager entityManager;

    public JornadaController(JornadaService service, EtapaJornadaService etapas,
            TarefaJornadaService tarefas, EntityManager entityManager) {
        this.service = service;
        this.etapas = etapas;
        this.tarefas = tarefas;
        this.entityManager = entityManager;
    }

    @PostMapping("/jornadas")
    @ResponseStatus(HttpStatus.CREATED)
    public JornadaResponse cadastrar(@RequestBody JornadaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @PostMapping("/jornadas/guia")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    public JornadaResponse criarGuia(@RequestBody GuiaJornadaRequest request, Authentication authentication) {
        if (request.etapas() == null || request.etapas().isEmpty() || request.etapas().size() > 30
            || request.etapas().stream().anyMatch(item -> item == null || item.etapa() == null
                || item.tarefas() == null || item.tarefas().size() > 30 || item.tarefas().stream().anyMatch(java.util.Objects::isNull))) {
            throw new RegraNegocioException("Informe etapas e tarefas válidas para a jornada", HttpStatus.BAD_REQUEST);
        }
        Integer usuario = usuario(authentication);
        if (entityManager.find(User.class, usuario, LockModeType.PESSIMISTIC_WRITE) == null) {
            throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        var existente = service.listarPorUsuario(usuario).stream()
            .filter(item -> java.util.Objects.equals(item.nome(), request.nome())
                && java.util.Objects.equals(item.descricao(), request.descricao())).findFirst();
        if (existente.isPresent()) return existente.get();
        JornadaResponse guia = service.cadastrar(new JornadaRequest(request.nome(), request.descricao(), null), usuario);
        for (var item : request.etapas()) {
            var etapa = etapas.cadastrar(guia.id(), item.etapa(), usuario);
            for (var tarefa : item.tarefas()) tarefas.cadastrar(etapa.id(), tarefa, usuario);
        }
        return guia;
    }

    @GetMapping("/jornadas/{id}")
    public JornadaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/jornadas/user/{userId}")
    public List<JornadaResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/jornadas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody JornadaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/jornadas/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/jornadas/{id}/reabrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reabrir(@PathVariable Integer id, Authentication authentication) {
        service.reabrir(id, usuario(authentication));
    }

    @DeleteMapping("/jornadas/{id}")
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

    @GetMapping("/jornadas/{id}/progresso")
    public JornadaProgressoResponse progresso(@PathVariable Integer id, Authentication authentication) {
        return service.progresso(id, usuario(authentication));
    }
}
