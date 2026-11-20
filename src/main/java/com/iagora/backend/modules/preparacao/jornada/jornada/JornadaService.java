package com.iagora.backend.modules.preparacao.jornada.jornada;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JornadaService {
    private final EtapaJornadaRepository etapaJornadaRepository;
    private final JornadaRepository repository;
    private final UserRepository userRepository;

    public JornadaService(EtapaJornadaRepository etapaJornadaRepository, JornadaRepository repository,
                          UserRepository userRepository) {
        this.etapaJornadaRepository = etapaJornadaRepository;
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(Jornada item) {
        return item.getUser().getId();
    }

    @Transactional
    public Jornada buscarDoUsuario(Integer id, Integer userId) {
        Jornada item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public JornadaResponse cadastrar(JornadaRequest request, Integer userId) {
        Jornada item = new Jornada();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        item.setCriadaEm(LocalDateTime.now());
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public JornadaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<JornadaResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, JornadaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        Jornada item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(Jornada item, JornadaRequest request, Integer userId) {
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
    }

    private JornadaResponse resposta(Jornada item) {
        return new JornadaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getDescricao(),
            item.getStatus(),
            item.getCriadaEm()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        Jornada item = buscarDoUsuario(id, userId);
        item.setStatus("concluida");
    }

    @Transactional
    public void reabrir(Integer id, Integer userId) {
        Jornada item = buscarDoUsuario(id, userId);
        item.setStatus("em_andamento");
    }

    @Transactional
    public JornadaProgressoResponse progresso(Integer id, Integer userId) {
        buscarDoUsuario(id, userId);
        var itens = etapaJornadaRepository.findByJornada_IdOrderByOrdemAscIdAsc(id);
        long concluidas = itens.stream().filter(item -> "concluida".equals(item.getStatus())).count();
        int percentual = itens.isEmpty() ? 0 : (int) Math.round(100.0 * concluidas / itens.size());
        return new JornadaProgressoResponse(itens.size(), concluidas, percentual);
    }
}
