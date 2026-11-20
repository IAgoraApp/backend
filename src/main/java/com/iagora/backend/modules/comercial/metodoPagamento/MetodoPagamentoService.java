package com.iagora.backend.modules.comercial.metodoPagamento;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MetodoPagamentoService {
    private final MetodoPagamentoRepository repository;
    private final EntityManager entityManager;

    public MetodoPagamentoService(MetodoPagamentoRepository repository, EntityManager entityManager) {
        this.repository = repository;
        this.entityManager = entityManager;
    }

    public MetodoPagamento buscar(Integer id) {
        if (id == null) {
            throw new RegraNegocioException("Informe o identificador", HttpStatus.BAD_REQUEST);
        }
        return repository.findById(id)
            .orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
    }

    @Transactional
    public MetodoPagamentoResponse cadastrar(MetodoPagamentoRequest request, Integer userId) {
        var item = new MetodoPagamento();
        item.setUser(bloquearUsuario(userId));
        validar(request);
        item.setTipo(request.tipo().strip());
        item.setApelido(apelido(request.apelido()));
        item.setBandeira(request.bandeira());
        item.setUltimosDigitos(request.ultimosDigitos());
        item.setTokenGateway(request.tokenGateway());
        item.setValidadeMes(request.validadeMes());
        item.setValidadeAno(request.validadeAno());
        return resposta(repository.save(item));
    }

    @Transactional
    public List<MetodoPagamentoResponse> listar(Integer userId) {
        return repository.findByUser_IdOrderByIdDesc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public MetodoPagamentoResponse consultar(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public void atualizar(Integer id, ApelidoMetodoPagamentoRequest request, Integer userId) {
        buscarDoUsuario(id, userId).setApelido(apelido(request.apelido()));
    }

    @Transactional
    public void principal(Integer id, Integer userId) {
        bloquearUsuario(userId);
        var item = buscarDoUsuario(id, userId);
        if (!item.getAtivo()) {
            throw new RegraNegocioException("Método de pagamento inativo", HttpStatus.CONFLICT);
        }
        repository.findByUser_IdAndPrincipalTrue(userId).forEach(metodo -> metodo.setPrincipal(false));
        item.setPrincipal(true);
    }

    @Transactional
    public void desativar(Integer id, Integer userId) {
        bloquearUsuario(userId);
        var item = buscarDoUsuario(id, userId);
        item.setAtivo(false);
        item.setPrincipal(false);
    }

    public MetodoPagamento buscarDoUsuario(Integer id, Integer userId) {
        var item = buscar(id);
        if (!item.getUser().getId().equals(userId)) {
            throw new RegraNegocioException("Método de pagamento pertence a outro usuário", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    private User bloquearUsuario(Integer userId) {
        var user = entityManager.find(User.class, userId, LockModeType.PESSIMISTIC_WRITE);
        if (user == null) {
            throw new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND);
        }
        return user;
    }

    private String apelido(String valor) {
        if (valor != null && valor.strip().length() > 100) {
            throw new RegraNegocioException("Apelido inválido", HttpStatus.BAD_REQUEST);
        }
        return valor == null ? null : valor.strip();
    }

    private void validar(MetodoPagamentoRequest request) {
        if (request.tipo() == null || request.tipo().isBlank() || request.tipo().length() > 30
            || (request.bandeira() != null && request.bandeira().length() > 30)) {
            throw new RegraNegocioException("Tipo ou bandeira inválidos", HttpStatus.BAD_REQUEST);
        }
        if (request.tokenGateway() == null || request.tokenGateway().isBlank() || request.tokenGateway().length() > 255
            || request.tokenGateway().matches("[0-9]{12,19}")) {
            throw new RegraNegocioException("Informe um token do gateway", HttpStatus.BAD_REQUEST);
        }
        if (request.ultimosDigitos() != null && !request.ultimosDigitos().matches("[0-9]{4}")) {
            throw new RegraNegocioException("Informe somente os quatro últimos dígitos", HttpStatus.BAD_REQUEST);
        }
        if ((request.validadeMes() != null && (request.validadeMes() < 1 || request.validadeMes() > 12))
            || (request.validadeAno() != null && (request.validadeAno() < 2000 || request.validadeAno() > 9999))) {
            throw new RegraNegocioException("Validade inválida", HttpStatus.BAD_REQUEST);
        }
    }

    public MetodoPagamentoResponse resposta(MetodoPagamento item) {
        return new MetodoPagamentoResponse(
            item.getId(),
            item.getUser().getId(),
            item.getTipo(),
            item.getApelido(),
            item.getBandeira(),
            item.getUltimosDigitos(),
            item.getValidadeMes(),
            item.getValidadeAno(),
            item.getPrincipal(),
            item.getAtivo()
        );
    }
}

