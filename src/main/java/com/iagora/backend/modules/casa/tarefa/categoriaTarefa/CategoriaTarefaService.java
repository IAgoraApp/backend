package com.iagora.backend.modules.casa.tarefa.categoriaTarefa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaRepository;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoriaTarefaService {
    private final CategoriaTarefaRepository repository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;
    private final TarefaRepository tarefaRepository;

    public CategoriaTarefaService(EntityManager entityManager, CategoriaTarefaRepository repository,
                                  UserRepository userRepository, TarefaRepository tarefaRepository) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
        this.tarefaRepository = tarefaRepository;
    }

    public Integer proprietarioId(CategoriaTarefa item) {
        return item.getUser().getId();
    }

    @Transactional
    public CategoriaTarefa buscarDoUsuario(Integer id, Integer userId) {
        CategoriaTarefa item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public CategoriaTarefaResponse cadastrar(CategoriaTarefaRequest request, Integer userId) {
        CategoriaTarefa item = new CategoriaTarefa();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public CategoriaTarefaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<CategoriaTarefaResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, CategoriaTarefaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        CategoriaTarefa item = buscarDoUsuario(id, userId);
        entityManager.lock(item, LockModeType.PESSIMISTIC_WRITE);
        if (tarefaRepository.existsByCategoria_Id(id)) {
            throw new RegraNegocioException("A categoria possui tarefas vinculadas", HttpStatus.CONFLICT);
        }
        repository.delete(item);
    }

    private void aplicar(CategoriaTarefa item, CategoriaTarefaRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 100) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.cor() != null) {
            if (request.cor().strip().length() > 20) {
                throw new RegraNegocioException("Campo cor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCor(request.cor().strip());
        }
        if (request.icone() != null) {
            if (request.icone().strip().length() > 100) {
                throw new RegraNegocioException("Campo icone inválido", HttpStatus.BAD_REQUEST);
            }
            item.setIcone(request.icone().strip());
        }
    }

    private CategoriaTarefaResponse resposta(CategoriaTarefa item) {
        return new CategoriaTarefaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getCor(),
            item.getIcone()
        );
    }
}
