package com.iagora.backend.modules.casa.tarefa.tarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.tarefa.categoriaTarefa.CategoriaTarefaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TarefaService {
    private final TarefaRepository repository;
    private final EntityManager entityManager;
    private final CategoriaTarefaService categoriaService;

    public TarefaService(EntityManager entityManager, TarefaRepository repository, CategoriaTarefaService categoriaService) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.categoriaService = categoriaService;
    }

    public Integer proprietarioId(Tarefa item) {
        return categoriaService.proprietarioId(item.getCategoria());
    }

    @Transactional
    public Tarefa buscarDoUsuario(Integer id, Integer userId) {
        Tarefa item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public TarefaResponse cadastrar(TarefaRequest request, Integer userId) {
        Tarefa item = new Tarefa();
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public TarefaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<TarefaResponse> listarPorUsuario(Integer userId) {
        return repository.findByCategoria_User_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public List<TarefaResponse> listarPorCategoria(Integer categoriaId, Integer userId) {
        categoriaService.buscarDoUsuario(categoriaId, userId);
        return repository.findByCategoria_IdOrderByIdAsc(categoriaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, TarefaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        Tarefa item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(Tarefa item, TarefaRequest request, Integer userId) {
        if (request.categoriaId() != null) {
            var categoria = categoriaService.buscarDoUsuario(request.categoriaId(), userId);
            entityManager.lock(categoria, LockModeType.PESSIMISTIC_WRITE);
            item.setCategoria(categoria);
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoriaId obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.descricao() != null) {
            item.setDescricao(request.descricao().strip());
        }
        if (request.prazo() != null) {
            item.setPrazo(request.prazo());
        }
    }

    private TarefaResponse resposta(Tarefa item) {
        return new TarefaResponse(
            item.getId(),
            item.getCategoria().getId(),
            item.getNome(),
            item.getDescricao(),
            item.getPrazo()
        );
    }
}
