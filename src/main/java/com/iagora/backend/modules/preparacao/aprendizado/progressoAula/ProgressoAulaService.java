package com.iagora.backend.modules.preparacao.aprendizado.progressoAula;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaRepository;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ProgressoAulaService {
    private final ProgressoAulaRepository repository;
    private final AulaRepository aulaRepository;
    private final AulaService aulaService;
    private final EntityManager entityManager;

    public ProgressoAulaService(ProgressoAulaRepository repository, AulaRepository aulaRepository,
                                AulaService aulaService, EntityManager entityManager) {
        this.repository = repository;
        this.aulaRepository = aulaRepository;
        this.aulaService = aulaService;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<ProgressoAulaResponse> listar(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream()
            .map(item -> new ProgressoAulaResponse(item.getAula().getId(), item.getConcluida())).toList();
    }

    @Transactional
    public ProgressoAulaResumoResponse resumo(Integer userId) {
        long total = aulaRepository.count();
        long concluidas = listar(userId).stream().filter(ProgressoAulaResponse::concluida).count();
        int percentual = total == 0 ? 0 : (int) Math.round(100.0 * concluidas / total);
        return new ProgressoAulaResumoResponse(total, concluidas, percentual);
    }

    @Transactional
    public ProgressoAulaResponse consultar(Integer aulaId, Integer userId) {
        aulaService.buscar(aulaId);
        return new ProgressoAulaResponse(aulaId,
            repository.findByUser_IdAndAula_Id(userId, aulaId).map(ProgressoAula::getConcluida).orElse(false));
    }

    @Transactional
    public void concluir(Integer aulaId, Integer userId) {
        atualizar(aulaId, userId, true);
    }

    @Transactional
    public void reabrir(Integer aulaId, Integer userId) {
        atualizar(aulaId, userId, false);
    }

    private void atualizar(Integer aulaId, Integer userId, boolean concluida) {
        var aula = aulaService.buscar(aulaId);
        User user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        var progresso = repository.findByUser_IdAndAula_Id(userId, aulaId).orElseGet(() -> {
            var novo = new ProgressoAula();
            novo.setUser(user);
            novo.setAula(aula);
            return novo;
        });
        progresso.setConcluida(concluida);
        repository.save(progresso);
    }
}
