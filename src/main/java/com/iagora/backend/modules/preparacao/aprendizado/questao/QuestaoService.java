package com.iagora.backend.modules.preparacao.aprendizado.questao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class QuestaoService {
    private final QuestaoRepository repository;
    private final QuizService quizService;

    public QuestaoService(QuestaoRepository repository, QuizService quizService) {
        this.repository = repository;
        this.quizService = quizService;
    }

    public Questao buscar(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public QuestaoResponse buscarPorId(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public List<QuestaoResponse> listar(Integer quizId) {
        quizService.buscar(quizId);
        return repository.findByQuiz_IdOrderByIdAsc(quizId).stream().map(this::resposta).toList();
    }

    @Transactional
    public QuestaoResponse cadastrar(Integer quizId, QuestaoRequest request) {
        var item = new Questao();
        item.setQuiz(quizService.buscar(quizId));
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, QuestaoRequest request) {
        aplicar(buscar(id), request);
    }

    @Transactional
    public void apagar(Integer id) {
        repository.delete(buscar(id));
    }

    private void aplicar(Questao item, QuestaoRequest request) {
        if (request.enunciado() != null) {
            if (request.enunciado().isBlank()) {
                throw new RegraNegocioException("Campo enunciado inválido", HttpStatus.BAD_REQUEST);
            }
            item.setEnunciado(request.enunciado().strip());
        }
        if (item.getEnunciado() == null) {
            throw new RegraNegocioException("Campo enunciado obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private QuestaoResponse resposta(Questao item) {
        return new QuestaoResponse(item.getId(), item.getQuiz().getId(), item.getEnunciado());
    }
}
