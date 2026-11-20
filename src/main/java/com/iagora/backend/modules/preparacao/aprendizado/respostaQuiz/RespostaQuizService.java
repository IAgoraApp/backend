package com.iagora.backend.modules.preparacao.aprendizado.respostaQuiz;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.aprendizado.alternativa.AlternativaService;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoService;
import com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz.TentativaQuiz;
import com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz.TentativaQuizService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class RespostaQuizService {
    private final RespostaQuizRepository repository;
    private final TentativaQuizService tentativaService;
    private final QuestaoService questaoService;
    private final AlternativaService alternativaService;
    private final EntityManager entityManager;

    public RespostaQuizService(RespostaQuizRepository repository, TentativaQuizService tentativaService,
                               QuestaoService questaoService, AlternativaService alternativaService,
                               EntityManager entityManager) {
        this.repository = repository;
        this.tentativaService = tentativaService;
        this.questaoService = questaoService;
        this.alternativaService = alternativaService;
        this.entityManager = entityManager;
    }

    @Transactional
    public RespostaQuizResponse cadastrar(Integer tentativaId, RespostaQuizRequest request, Integer userId) {
        var tentativa = tentativaService.buscar(tentativaId, userId, true);
        validarAberta(tentativa);
        if (request.questaoId() == null) {
            throw new RegraNegocioException("Informe a questão", HttpStatus.BAD_REQUEST);
        }
        var questao = questaoService.buscar(request.questaoId());
        if (!questao.getQuiz().getId().equals(tentativa.getQuiz().getId())) {
            throw new RegraNegocioException("A questão deve pertencer ao quiz", HttpStatus.BAD_REQUEST);
        }
        var item = new RespostaQuiz();
        item.setTentativa(tentativa);
        item.setQuestao(questao);
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public List<RespostaQuizResponse> listar(Integer tentativaId, Integer userId) {
        tentativaService.buscar(tentativaId, userId, false);
        return repository.findByTentativa_IdOrderByIdAsc(tentativaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, RespostaQuizRequest request, Integer userId) {
        aplicar(buscarEditavel(id, userId), request);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        repository.delete(buscarEditavel(id, userId));
    }

    private RespostaQuiz buscarEditavel(Integer id, Integer userId) {
        var item = repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Resposta não encontrada", HttpStatus.NOT_FOUND));
        validarAberta(tentativaService.buscar(item.getTentativa().getId(), userId, true));
        entityManager.refresh(item);
        return item;
    }

    private void aplicar(RespostaQuiz item, RespostaQuizRequest request) {
        if (request.questaoId() != null && !item.getQuestao().getId().equals(request.questaoId())) {
            throw new RegraNegocioException("A questão da resposta é fixa", HttpStatus.BAD_REQUEST);
        }
        if (request.alternativaId() == null) {
            throw new RegraNegocioException("Informe a alternativa", HttpStatus.BAD_REQUEST);
        }
        var alternativa = alternativaService.buscar(request.alternativaId());
        if (!alternativa.getQuestao().getId().equals(item.getQuestao().getId())) {
            throw new RegraNegocioException("A alternativa deve pertencer à questão", HttpStatus.BAD_REQUEST);
        }
        item.setAlternativa(alternativa);
    }

    private void validarAberta(TentativaQuiz tentativa) {
        if (tentativa.getPontuacao() != null) {
            throw new RegraNegocioException("Tentativa já finalizada", HttpStatus.CONFLICT);
        }
    }

    private RespostaQuizResponse resposta(RespostaQuiz item) {
        return new RespostaQuizResponse(item.getId(), item.getTentativa().getId(), item.getQuestao().getId(),
            item.getAlternativa().getId());
    }
}
