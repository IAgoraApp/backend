package com.iagora.backend.modules.marketplace.servicoFavorito;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.servico.ServicoResponse;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ServicoFavoritoService {
    private final ServicoFavoritoRepository repository;
    private final ServicoService servicoService;
    private final EntityManager entityManager;

    public ServicoFavoritoService(ServicoFavoritoRepository repository, ServicoService servicoService,
                                  EntityManager entityManager) {
        this.repository = repository;
        this.servicoService = servicoService;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<ServicoResponse> listar(Integer userId) {
        var agora = LocalDateTime.now();
        return repository.findByUser_IdOrderByServico_IdAsc(userId).stream()
            .map(ServicoFavorito::getServico)
            .filter(item -> servicoService.visivel(item, agora))
            .map(servicoService::resposta).toList();
    }

    @Transactional
    public void adicionar(Integer id, Integer userId) {
        var user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        var servico = servicoService.buscarVisivel(id);
        if (repository.findByUser_IdAndServico_Id(userId, id).isEmpty()) {
            var item = new ServicoFavorito();
            item.setUser(user);
            item.setServico(servico);
            repository.save(item);
        }
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        repository.findByUser_IdAndServico_Id(userId, id).ifPresent(repository::delete);
    }
}

