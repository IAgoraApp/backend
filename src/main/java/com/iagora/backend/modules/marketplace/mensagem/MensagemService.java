package com.iagora.backend.modules.marketplace.mensagem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.chatServico.ChatServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MensagemService {
    private final MensagemRepository repository;
    private final ChatServicoService chatService;
    private final EntityManager entityManager;

    public MensagemService(MensagemRepository repository, ChatServicoService chatService, EntityManager entityManager) {
        this.repository = repository;
        this.chatService = chatService;
        this.entityManager = entityManager;
    }

    @Transactional
    public MensagemResponse cadastrar(Integer chatId, MensagemRequest request, Integer userId) {
        var chat = chatService.buscarParticipante(chatId, userId);
        entityManager.refresh(chat, LockModeType.PESSIMISTIC_WRITE);
        if (request.mensagem() == null || request.mensagem().isBlank() || request.mensagem().length() > 4000) {
            throw new RegraNegocioException("Informe uma mensagem com até 4.000 caracteres", HttpStatus.BAD_REQUEST);
        }
        var item = new Mensagem();
        item.setChat(chat);
        item.setUser(entityManager.getReference(User.class, userId));
        item.setMensagem(request.mensagem().strip());
        item.setData(LocalDateTime.now());
        chat.setOcultoCliente(false);
        chat.setOcultoEmpresa(false);
        return resposta(repository.save(item));
    }

    @Transactional
    public List<MensagemResponse> listar(Integer chatId, Integer userId) {
        chatService.buscarParticipante(chatId, userId);
        return repository.findByChat_IdOrderByIdAsc(chatId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void ler(Integer chatId, Integer userId) {
        chatService.buscarParticipante(chatId, userId);
        LocalDateTime agora = LocalDateTime.now();
        repository.findByChat_IdAndUser_IdNotAndLidaEmIsNull(chatId, userId)
            .forEach(item -> item.setLidaEm(agora));
    }

    @Transactional
    public MensagensNaoLidasResponse naoLidas(Integer chatId, Integer userId) {
        chatService.buscarParticipante(chatId, userId);
        return new MensagensNaoLidasResponse(repository.countByChat_IdAndUser_IdNotAndLidaEmIsNull(chatId, userId));
    }

    public MensagemResponse resposta(Mensagem item) {
        return new MensagemResponse(
            item.getId(),
            item.getChat().getId(),
            item.getUser().getId(),
            item.getMensagem(),
            item.getData(),
            item.getLidaEm()
        );
    }
}

