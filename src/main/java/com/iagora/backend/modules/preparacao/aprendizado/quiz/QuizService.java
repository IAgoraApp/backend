package com.iagora.backend.modules.preparacao.aprendizado.quiz;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class QuizService {
    private final QuizRepository repository;
    private final AulaService aulaService;

    public QuizService(QuizRepository repository, AulaService aulaService) {
        this.repository = repository;
        this.aulaService = aulaService;
    }

    public Quiz buscar(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public QuizResponse buscarPorId(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public List<QuizResponse> listar(Integer aulaId) {
        aulaService.buscar(aulaId);
        return repository.findByAula_IdOrderByIdAsc(aulaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public QuizResponse cadastrar(Integer aulaId, QuizRequest request) {
        var item = new Quiz();
        item.setAula(aulaService.buscar(aulaId));
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, QuizRequest request) {
        aplicar(buscar(id), request);
    }

    @Transactional
    public void apagar(Integer id) {
        repository.delete(buscar(id));
    }

    private void aplicar(Quiz item, QuizRequest request) {
        if (request.titulo() != null) {
            if (request.titulo().isBlank() || request.titulo().strip().length() > 150) {
                throw new RegraNegocioException("Campo titulo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTitulo(request.titulo().strip());
        }
        if (item.getTitulo() == null) {
            throw new RegraNegocioException("Campo titulo obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private QuizResponse resposta(Quiz item) {
        return new QuizResponse(item.getId(), item.getAula().getId(), item.getTitulo());
    }
}
