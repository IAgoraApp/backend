package com.iagora.backend.modules.preparacao.dinheiroPreparacao;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;

@Service
public class DinheiroPreparacaoService {

    private final DinheiroPreparacaoRepository repository;
    private final UserRepository userRepository;
    private final EntityManager entityManager;

    public DinheiroPreparacaoService(DinheiroPreparacaoRepository repository, UserRepository userRepository,
                                     EntityManager entityManager) {
        this.repository = repository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
    }

    @Transactional
    public DinheiroPreparacaoResponse consultar(Integer userId) {
        return repository.findById(userId)
            .map(item -> new DinheiroPreparacaoResponse(item.getRenda(), item.getReserva(), item.getMudancaPrevista()))
            .orElseGet(() -> new DinheiroPreparacaoResponse(null, null, null));
    }

    @Transactional
    public void atualizar(Integer userId, DinheiroPreparacaoRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RegraNegocioException("Usuário não encontrado", HttpStatus.NOT_FOUND));
        entityManager.lock(user, LockModeType.PESSIMISTIC_WRITE);
        DinheiroPreparacao item = repository.findById(userId).orElseGet(() -> new DinheiroPreparacao(user));
        if (request.renda() != null) {
            item.setRenda(valor(request.renda(), "renda"));
        }
        if (request.reserva() != null) {
            item.setReserva(valor(request.reserva(), "reserva"));
        }
        if (request.mudancaPrevista() != null) {
            item.setMudancaPrevista(data(request.mudancaPrevista()));
        }
        repository.save(item);
    }

    private BigDecimal valor(JsonNode node, String campo) {
        if (node.isNull()) {
            return null;
        }
        if (node.isNumber()) {
            BigDecimal valor = node.decimalValue();
            if (valor.signum() >= 0 && valor.scale() <= 2 && valor.precision() - valor.scale() <= 10) {
                return valor;
            }
        }
        throw new RegraNegocioException("Campo " + campo + " inválido", HttpStatus.BAD_REQUEST);
    }

    private LocalDate data(JsonNode node) {
        if (node.isNull()) {
            return null;
        }
        try {
            return LocalDate.parse(node.asText());
        } catch (DateTimeParseException ex) {
            throw new RegraNegocioException("Previsão de mudança inválida", HttpStatus.BAD_REQUEST);
        }
    }
}
