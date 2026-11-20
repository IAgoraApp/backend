package com.iagora.backend.modules.preparacao.gastoPreparacao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class GastoPreparacaoService {
    private final GastoPreparacaoRepository repository;
    private final UserRepository userRepository;

    public GastoPreparacaoService(GastoPreparacaoRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(GastoPreparacao item) {
        return item.getUser().getId();
    }

    @Transactional
    public GastoPreparacao buscarDoUsuario(Integer id, Integer userId) {
        GastoPreparacao item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public GastoPreparacaoResponse cadastrar(GastoPreparacaoRequest request, Integer userId) {
        GastoPreparacao item = new GastoPreparacao();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public GastoPreparacaoResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<GastoPreparacaoResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, GastoPreparacaoRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        GastoPreparacao item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(GastoPreparacao item, GastoPreparacaoRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 100) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.valor() != null) {
            if (request.valor().scale() > 2 || request.valor().precision() - request.valor().scale() > 10 || request.valor().signum() < 0) {
                throw new RegraNegocioException("Campo valor inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValor(request.valor());
        }
        if (item.getValor() == null) {
            throw new RegraNegocioException("Campo valor obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.frequencia() != null) {
            if (request.frequencia().isBlank() || request.frequencia().strip().length() > 20) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("fixo", "variavel").contains(request.frequencia())) {
                throw new RegraNegocioException("Campo frequencia inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFrequencia(request.frequencia().strip());
        }
        if (item.getFrequencia() == null) {
            throw new RegraNegocioException("Campo frequencia obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.destino() != null) {
            if (request.destino().isBlank() || request.destino().strip().length() > 20) {
                throw new RegraNegocioException("Campo destino inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("continua", "termina", "transferido").contains(request.destino())) {
                throw new RegraNegocioException("Campo destino inválido", HttpStatus.BAD_REQUEST);
            }
            item.setDestino(request.destino().strip());
        }
        if (request.categoria() != null) {
            String categoria = request.categoria().strip();
            if (categoria.length() > 50) {
                throw new RegraNegocioException("Categoria inválida", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(categoria.isEmpty() ? null : categoria);
        }
        if (item.getDestino() == null) {
            throw new RegraNegocioException("Campo destino obrigatório", HttpStatus.BAD_REQUEST);
        }
    }

    private GastoPreparacaoResponse resposta(GastoPreparacao item) {
        return new GastoPreparacaoResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getValor(),
            item.getFrequencia(),
            item.getDestino(),
            item.getCategoria()
        );
    }
}
