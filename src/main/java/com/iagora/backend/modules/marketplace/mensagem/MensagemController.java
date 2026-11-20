package com.iagora.backend.modules.marketplace.mensagem;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Mensagem", description = "Operações de Mensagem.")
public class MensagemController {
    private final MensagemService service;

    public MensagemController(MensagemService service) {
        this.service = service;
    }

    @PostMapping("/chats-servico/{chatId}/mensagens")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse cadastrar(@PathVariable Integer chatId, @RequestBody MensagemRequest request, Authentication authentication) {
        return service.cadastrar(chatId, request, usuario(authentication));
    }

    @GetMapping("/chats-servico/{chatId}/mensagens")
    public List<MensagemResponse> listar(@PathVariable Integer chatId, Authentication authentication) {
        return service.listar(chatId, usuario(authentication));
    }

    @PatchMapping("/chats-servico/{chatId}/mensagens/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ler(@PathVariable Integer chatId, Authentication authentication) {
        service.ler(chatId, usuario(authentication));
    }

    @GetMapping("/chats-servico/{chatId}/mensagens/nao-lidas")
    public MensagensNaoLidasResponse naoLidas(@PathVariable Integer chatId, Authentication authentication) {
        return service.naoLidas(chatId, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
