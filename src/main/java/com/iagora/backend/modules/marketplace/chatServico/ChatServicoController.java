package com.iagora.backend.modules.marketplace.chatServico;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "ChatServico", description = "Operações de ChatServico.")
public class ChatServicoController {
    private final ChatServicoService service;

    public ChatServicoController(ChatServicoService service) {
        this.service = service;
    }

    @PostMapping("/chats-servico")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatServicoResponse cadastrar(@RequestBody ChatServicoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/chats-servico")
    public List<ChatServicoResponse> listar(Authentication authentication) {
        return service.listar(usuario(authentication));
    }

    @GetMapping("/chats-servico/{id}")
    public ChatServicoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @DeleteMapping("/chats-servico/{id}")
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
