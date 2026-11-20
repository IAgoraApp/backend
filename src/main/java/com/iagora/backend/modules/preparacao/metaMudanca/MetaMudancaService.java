package com.iagora.backend.modules.preparacao.metaMudanca;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MetaMudancaService {
    private final MetaMudancaRepository repository;
    private final UserRepository userRepository;

    public MetaMudancaService(MetaMudancaRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(MetaMudanca item) {
        return item.getUser().getId();
    }

    @Transactional
    public MetaMudanca buscarDoUsuario(Integer id, Integer userId) {
        MetaMudanca item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public MetaMudancaResponse cadastrar(MetaMudancaRequest request, Integer userId) {
        MetaMudanca item = new MetaMudanca();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public MetaMudancaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<MetaMudancaResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, MetaMudancaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        MetaMudanca item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(MetaMudanca item, MetaMudancaRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.fonte() != null) {
            if (request.fonte().isBlank() || request.fonte().strip().length() > 20) {
                throw new RegraNegocioException("Campo fonte inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("personalizada", "mudanca").contains(request.fonte())) {
                throw new RegraNegocioException("Campo fonte inválido", HttpStatus.BAD_REQUEST);
            }
            item.setFonte(request.fonte().strip());
        }
        if (item.getFonte() == null) {
            throw new RegraNegocioException("Campo fonte obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.alvo() != null) {
            if (request.alvo().scale() > 2 || request.alvo().precision() - request.alvo().scale() > 10 || request.alvo().signum() < 0) {
                throw new RegraNegocioException("Campo alvo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setAlvo(request.alvo());
        }
        if (item.getAlvo() == null) {
            throw new RegraNegocioException("Campo alvo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.modo() != null) {
            if (request.modo().isBlank() || request.modo().strip().length() > 20) {
                throw new RegraNegocioException("Campo modo inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("prazo", "aporte").contains(request.modo())) {
                throw new RegraNegocioException("Campo modo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setModo(request.modo().strip());
        }
        if (item.getModo() == null) {
            throw new RegraNegocioException("Campo modo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.data() != null) {
            item.setData(request.data());
        }
        if (request.aporte() != null) {
            if (request.aporte().scale() > 2 || request.aporte().precision() - request.aporte().scale() > 10 || request.aporte().signum() < 0) {
                throw new RegraNegocioException("Campo aporte inválido", HttpStatus.BAD_REQUEST);
            }
            item.setAporte(request.aporte());
        }
        if (request.estimativaMudanca() != null) {
            item.setEstimativaMudanca(request.estimativaMudanca());
        }
        if ("prazo".equals(item.getModo()) && item.getData() == null) {
            throw new RegraNegocioException("Informe a data da meta", HttpStatus.BAD_REQUEST);
        }
        if ("aporte".equals(item.getModo()) && (item.getAporte() == null || item.getAporte().signum() <= 0)) {
            throw new RegraNegocioException("Informe um aporte positivo", HttpStatus.BAD_REQUEST);
        }
    }

    private MetaMudancaResponse resposta(MetaMudanca item) {
        return new MetaMudancaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getFonte(),
            item.getAlvo(),
            item.getModo(),
            item.getData(),
            item.getAporte(),
            item.getEstimativaMudanca()
        );
    }
}
