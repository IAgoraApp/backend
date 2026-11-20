package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ExecucaoTarefaService {
    private final ExecucaoTarefaRepository repository;
    private final TarefaService tarefaService;

    public ExecucaoTarefaService(ExecucaoTarefaRepository repository, TarefaService tarefaService) {
        this.repository = repository;
        this.tarefaService = tarefaService;
    }

    public Integer proprietarioId(ExecucaoTarefa item) {
        return tarefaService.proprietarioId(item.getTarefa());
    }

    @Transactional
    public ExecucaoTarefa buscarDoUsuario(Integer id, Integer userId) {
        ExecucaoTarefa item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public ExecucaoTarefaResponse cadastrar(Integer tarefaId, ExecucaoTarefaRequest request, Integer userId) {
        ExecucaoTarefa item = new ExecucaoTarefa();
        item.setTarefa(tarefaService.buscarDoUsuario(tarefaId, userId));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public ExecucaoTarefaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<ExecucaoTarefaResponse> listarPorUsuario(Integer userId) {
        return repository.findByTarefa_Categoria_User_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public List<ExecucaoTarefaResponse> listarPorTarefa(Integer tarefaId, Integer userId) {
        tarefaService.buscarDoUsuario(tarefaId, userId);
        return repository.findByTarefa_IdOrderByIdAsc(tarefaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, ExecucaoTarefaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        ExecucaoTarefa item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(ExecucaoTarefa item, ExecucaoTarefaRequest request, Integer userId) {
        if (request.dataPrevista() != null) {
            item.setDataPrevista(request.dataPrevista());
        }
        if (item.getDataPrevista() == null) {
            throw new RegraNegocioException("Campo dataPrevista obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private ExecucaoTarefaResponse resposta(ExecucaoTarefa item) {
        return new ExecucaoTarefaResponse(
            item.getId(),
            item.getTarefa().getId(),
            item.getDataPrevista(),
            item.getConcluidaEm(),
            item.getStatus()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        ExecucaoTarefa item = buscarDoUsuario(id, userId);
        item.setStatus("concluida");
        item.setConcluidaEm(LocalDateTime.now());
    }

    @Transactional
    public void reabrir(Integer id, Integer userId) {
        ExecucaoTarefa item = buscarDoUsuario(id, userId);
        item.setStatus("pendente");
        item.setConcluidaEm(null);
    }
}
