package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaService;
import jakarta.transaction.Transactional;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class TarefaJornadaService {
    private final TarefaJornadaRepository repository;
    private final EtapaJornadaService etapaService;

    public TarefaJornadaService(TarefaJornadaRepository repository, EtapaJornadaService etapaService) {
        this.repository = repository;
        this.etapaService = etapaService;
    }

    public Integer proprietarioId(TarefaJornada item) {
        return etapaService.proprietarioId(item.getEtapa());
    }

    @Transactional
    public TarefaJornada buscarDoUsuario(Integer id, Integer userId) {
        TarefaJornada item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public TarefaJornadaResponse cadastrar(Integer etapaId, TarefaJornadaRequest request, Integer userId) {
        TarefaJornada item = new TarefaJornada();
        item.setEtapa(etapaService.buscarDoUsuario(etapaId, userId));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public TarefaJornadaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }


    @Transactional
    public List<TarefaJornadaResponse> listarPorEtapa(Integer etapaId, Integer userId) {
        etapaService.buscarDoUsuario(etapaId, userId);
        return repository.findByEtapa_IdOrderByOrdemAscIdAsc(etapaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, TarefaJornadaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        TarefaJornada item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(TarefaJornada item, TarefaJornadaRequest request, Integer userId) {
        if (request.titulo() != null) {
            if (request.titulo().isBlank() || request.titulo().strip().length() > 150) {
                throw new RegraNegocioException("Campo titulo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTitulo(request.titulo().strip());
        }
        if (item.getTitulo() == null) {
            throw new RegraNegocioException("Campo titulo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.descricao() != null) {
            item.setDescricao(request.descricao().strip());
        }
        if (request.ordem() != null) {
            if (request.ordem() < 0) {
                throw new RegraNegocioException("Campo ordem inválido", HttpStatus.BAD_REQUEST);
            }
            item.setOrdem(request.ordem());
        }
        if (item.getOrdem() == null) {
            throw new RegraNegocioException("Campo ordem obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private TarefaJornadaResponse resposta(TarefaJornada item) {
        return new TarefaJornadaResponse(
            item.getId(),
            item.getEtapa().getId(),
            item.getTitulo(),
            item.getDescricao(),
            item.getConcluida(),
            item.getOrdem()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        TarefaJornada item = buscarDoUsuario(id, userId);
        item.setConcluida(true);
    }

    @Transactional
    public void reabrir(Integer id, Integer userId) {
        TarefaJornada item = buscarDoUsuario(id, userId);
        item.setConcluida(false);
    }

    @Transactional
    public void ordenar(Integer etapaId, OrdenarTarefaJornadaRequest request, Integer userId) {
        etapaService.buscarDoUsuario(etapaId, userId);
        var itens = repository.findByEtapa_IdOrderByOrdemAscIdAsc(etapaId);
        var ids = request.ids();
        if (ids == null || ids.size() != itens.size()
            || !new HashSet<>(ids).equals(itens.stream().map(TarefaJornada::getId).collect(Collectors.toSet()))) {
            throw new RegraNegocioException("Informe todos os identificadores da lista, uma única vez", HttpStatus.BAD_REQUEST);
        }
        var posicoes = new HashMap<Integer, Integer>();
        for (int i = 0; i < ids.size(); i++) {
            posicoes.put(ids.get(i), i + 1);
        }
        itens.forEach(item -> item.setOrdem(posicoes.get(item.getId())));
    }
}
