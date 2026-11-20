package com.iagora.backend.modules.financas.metaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MetaFinanceiraService {
    private final MetaFinanceiraRepository repository;
    private final EntityManager entityManager;
    private final UserRepository userRepository;

    public MetaFinanceiraService(MetaFinanceiraRepository repository, UserRepository userRepository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
        this.userRepository = userRepository;
    }

    public Integer proprietarioId(MetaFinanceira item) {
        return item.getUser().getId();
    }

    @Transactional
    public MetaFinanceira buscarDoUsuario(Integer id, Integer userId) {
        MetaFinanceira item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public MetaFinanceiraResponse cadastrar(MetaFinanceiraRequest request, Integer userId) {
        MetaFinanceira item = new MetaFinanceira();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public MetaFinanceiraResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<MetaFinanceiraResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, MetaFinanceiraRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        MetaFinanceira item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(MetaFinanceira item, MetaFinanceiraRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.valorAtual() != null) {
            if (request.valorAtual().scale() > 2 || request.valorAtual().precision() - request.valorAtual().scale() > 13 || request.valorAtual().signum() < 0) {
                throw new RegraNegocioException("Campo valorAtual inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValorAtual(request.valorAtual());
        }
        if (request.valorAlvo() != null) {
            if (request.valorAlvo().scale() > 2 || request.valorAlvo().precision() - request.valorAlvo().scale() > 13 || request.valorAlvo().signum() < 0) {
                throw new RegraNegocioException("Campo valorAlvo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setValorAlvo(request.valorAlvo());
        }
        if (item.getValorAlvo() == null) {
            throw new RegraNegocioException("Campo valorAlvo obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.prazo() != null) {
            item.setPrazo(request.prazo());
        }
    }

    private MetaFinanceiraResponse resposta(MetaFinanceira item) {
        return new MetaFinanceiraResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getValorAtual(),
            item.getValorAlvo(),
            item.getPrazo(),
            item.getStatus()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        MetaFinanceira item = buscarDoUsuario(id, userId);
        item.setStatus("concluida");
    }

    @Transactional
    public void cancelar(Integer id, Integer userId) {
        MetaFinanceira item = buscarDoUsuario(id, userId);
        item.setStatus("cancelada");
    }

    @Transactional
    public void valor(Integer id, Integer userId, AporteMetaFinanceiraRequest data) {
        MetaFinanceira item = buscarDoUsuario(id, userId);
        if (data.valor() == null || data.valor().signum() <= 0 || data.valor().scale() > 2) {
            throw new RegraNegocioException("Informe um valor positivo com até duas casas decimais", HttpStatus.BAD_REQUEST);
        }
        entityManager.refresh(item, LockModeType.PESSIMISTIC_WRITE);
        BigDecimal saldo = item.getValorAtual().add(data.valor());
        if (saldo.precision() - saldo.scale() > 13) {
            throw new RegraNegocioException("Valor acumulado acima do limite", HttpStatus.BAD_REQUEST);
        }
        item.setValorAtual(saldo);
    }
}
