package com.iagora.backend.modules.preparacao.jornada.etapaJornada;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.jornada.jornada.JornadaService;
import com.iagora.backend.modules.preparacao.jornada.tarefaJornada.TarefaJornadaRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class EtapaJornadaService {
    private final TarefaJornadaRepository tarefaJornadaRepository;
    private final EtapaJornadaRepository repository;
    private final JornadaService jornadaService;

    public EtapaJornadaService(TarefaJornadaRepository tarefaJornadaRepository, EtapaJornadaRepository repository,
                               JornadaService jornadaService) {
        this.tarefaJornadaRepository = tarefaJornadaRepository;
        this.repository = repository;
        this.jornadaService = jornadaService;
    }

    public Integer proprietarioId(EtapaJornada item) {
        return jornadaService.proprietarioId(item.getJornada());
    }

    @Transactional
    public EtapaJornada buscarDoUsuario(Integer id, Integer userId) {
        EtapaJornada item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public EtapaJornadaResponse cadastrar(Integer jornadaId, EtapaJornadaRequest request, Integer userId) {
        EtapaJornada item = new EtapaJornada();
        item.setJornada(jornadaService.buscarDoUsuario(jornadaId, userId));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public EtapaJornadaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }


    @Transactional
    public List<EtapaJornadaResponse> listarPorJornada(Integer jornadaId, Integer userId) {
        jornadaService.buscarDoUsuario(jornadaId, userId);
        return repository.findByJornada_IdOrderByOrdemAscIdAsc(jornadaId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, EtapaJornadaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        EtapaJornada item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(EtapaJornada item, EtapaJornadaRequest request, Integer userId) {
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
        if (request.categoria() != null) {
            if (request.categoria().strip().length() > 50) {
                throw new RegraNegocioException("Campo categoria inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(request.categoria().strip());
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
        if (request.prazo() != null) {
            item.setPrazo(request.prazo());
        }
        if (request.foto() != null) {
            if (request.foto().strip().length() > 150) {
                throw new RegraNegocioException("Campo foto inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFoto(request.foto().strip());
        }
    }

    private EtapaJornadaResponse resposta(EtapaJornada item) {
        return new EtapaJornadaResponse(
            item.getId(),
            item.getJornada().getId(),
            item.getTitulo(),
            item.getDescricao(),
            item.getCategoria(),
            item.getOrdem(),
            item.getStatus(),
            item.getPrazo(),
            item.getConcluidaEm(),
            item.getFoto()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        EtapaJornada item = buscarDoUsuario(id, userId);
        item.setStatus("concluida");
        item.setConcluidaEm(LocalDateTime.now());
    }

    @Transactional
    public void reabrir(Integer id, Integer userId) {
        EtapaJornada item = buscarDoUsuario(id, userId);
        item.setStatus("pendente");
        item.setConcluidaEm(null);
    }

    @Transactional
    public void ordenar(Integer jornadaId, OrdenarEtapaJornadaRequest request, Integer userId) {
        jornadaService.buscarDoUsuario(jornadaId, userId);
        var itens = repository.findByJornada_IdOrderByOrdemAscIdAsc(jornadaId);
        var ids = request.ids();
        if (ids == null || ids.size() != itens.size()
            || !new HashSet<>(ids).equals(itens.stream().map(EtapaJornada::getId).collect(Collectors.toSet()))) {
            throw new RegraNegocioException("Informe todos os identificadores da lista, uma única vez", HttpStatus.BAD_REQUEST);
        }
        var posicoes = new HashMap<Integer, Integer>();
        for (int i = 0; i < ids.size(); i++) {
            posicoes.put(ids.get(i), i + 1);
        }
        itens.forEach(item -> item.setOrdem(posicoes.get(item.getId())));
    }

    @Transactional
    public EtapaJornadaProgressoResponse progresso(Integer id, Integer userId) {
        buscarDoUsuario(id, userId);
        var itens = tarefaJornadaRepository.findByEtapa_IdOrderByOrdemAscIdAsc(id);
        long concluidas = itens.stream().filter(item -> item.getConcluida()).count();
        int percentual = itens.isEmpty() ? 0 : (int) Math.round(100.0 * concluidas / itens.size());
        return new EtapaJornadaProgressoResponse(itens.size(), concluidas, percentual);
    }
}
