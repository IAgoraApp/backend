package com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoRepository;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizService;
import com.iagora.backend.modules.preparacao.aprendizado.respostaQuiz.RespostaQuizRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TentativaQuizService {
    private final TentativaQuizRepository repository;
    private final UserRepository userRepository;
    private final QuizService quizService;
    private final QuestaoRepository questaoRepository;
    private final RespostaQuizRepository respostaRepository;
    private final EntityManager entityManager;

    public TentativaQuizService(TentativaQuizRepository repository, UserRepository userRepository,
                                QuizService quizService, QuestaoRepository questaoRepository,
                                RespostaQuizRepository respostaRepository, EntityManager entityManager) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.quizService = quizService;
        this.questaoRepository = questaoRepository;
        this.respostaRepository = respostaRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public TentativaQuizResponse cadastrar(Integer quizId, Integer userId) {
        var item = new TentativaQuiz();
        item.setQuiz(quizService.buscar(quizId));
        item.setUser(userRepository.findById(userId)
            .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND)));
        return resposta(repository.save(item));
    }

    @Transactional
    public List<TentativaQuizResponse> listar(Integer quizId, Integer userId) {
        quizService.buscar(quizId);
        return repository.findByQuiz_IdAndUser_IdOrderByIdDesc(quizId, userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public TentativaQuizResponse consultar(Integer id, Integer userId) {
        return resposta(buscar(id, userId, false));
    }

    @Transactional
    public TentativaQuizResultadoResponse finalizar(Integer id, Integer userId) {
        TentativaQuiz item = buscar(id, userId, true);
        if (item.getPontuacao() == null) {
            int total = questaoRepository.findByQuiz_IdOrderByIdAsc(item.getQuiz().getId()).size();
            if (total == 0) {
                throw new RegraNegocioException("O quiz não possui questões", HttpStatus.CONFLICT);
            }
            int acertos = (int) respostaRepository.findByTentativa_IdOrderByIdAsc(id).stream()
                .filter(resposta -> resposta.getAlternativa().getCorreta()).count();
            item.setTotalQuestoes(total);
            item.setAcertos(acertos);
            item.setPontuacao(BigDecimal.valueOf(100L * acertos)
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP));
        }
        return resultado(item);
    }

    @Transactional
    public TentativaQuizResultadoResponse resultado(Integer id, Integer userId) {
        return resultado(buscar(id, userId, false));
    }

    public TentativaQuiz buscar(Integer id, Integer userId, boolean bloquear) {
        var item = bloquear
            ? entityManager.find(TentativaQuiz.class, id, LockModeType.PESSIMISTIC_WRITE)
            : repository.findById(id).orElse(null);
        if (item == null) {
            throw new RegraNegocioException("Tentativa não encontrada", HttpStatus.NOT_FOUND);
        }
        if (!item.getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Tentativa pertence a outro usuário", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    private TentativaQuizResultadoResponse resultado(TentativaQuiz item) {
        if (item.getPontuacao() == null) {
            throw new RegraNegocioException("Finalize a tentativa para ver o resultado", HttpStatus.CONFLICT);
        }
        return new TentativaQuizResultadoResponse(item.getId(), item.getPontuacao(), item.getTotalQuestoes(), item.getAcertos());
    }

    private TentativaQuizResponse resposta(TentativaQuiz item) {
        return new TentativaQuizResponse(item.getId(), item.getUser().getId(), item.getQuiz().getId(), item.getPontuacao());
    }
}
