package com.iagora.backend.modules.preparacao.aprendizado.alternativa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class AlternativaService {
    private final AlternativaRepository repository;
    private final QuestaoService questaoService;

    public AlternativaService(AlternativaRepository repository, QuestaoService questaoService) {
        this.repository = repository;
        this.questaoService = questaoService;
    }

    public Alternativa buscar(Integer id) {
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public AlternativaResponse buscarPorId(Integer id) {
        return resposta(buscar(id));
    }

    @Transactional
    public List<AlternativaResponse> listar(Integer questaoId) {
        questaoService.buscar(questaoId);
        return repository.findByQuestao_IdOrderByIdAsc(questaoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public AlternativaResponse cadastrar(Integer questaoId, AlternativaRequest request) {
        var item = new Alternativa();
        item.setQuestao(questaoService.buscar(questaoId));
        aplicar(item, request);
        return resposta(repository.save(item));
    }

    @Transactional
    public void atualizar(Integer id, AlternativaRequest request) {
        aplicar(buscar(id), request);
    }

    @Transactional
    public void apagar(Integer id) {
        repository.delete(buscar(id));
    }

    private void aplicar(Alternativa item, AlternativaRequest request) {
        if (request.texto() != null) {
            if (request.texto().isBlank()) {
                throw new RegraNegocioException("Campo texto inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTexto(request.texto().strip());
        }
        if (item.getTexto() == null) {
            throw new RegraNegocioException("Campo texto obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.correta() != null) {
            item.setCorreta(request.correta());
        }
    }

    private AlternativaResponse resposta(Alternativa item) {
        return new AlternativaResponse(item.getId(), item.getQuestao().getId(), item.getTexto());
    }
}
