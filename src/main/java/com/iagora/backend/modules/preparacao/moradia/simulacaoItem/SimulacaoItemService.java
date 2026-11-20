package com.iagora.backend.modules.preparacao.moradia.simulacaoItem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia.SimulacaoMoradiaService;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SimulacaoItemService {
    private final SimulacaoItemRepository repository;
    private final SimulacaoMoradiaService simulacaoService;

    public SimulacaoItemService(SimulacaoItemRepository repository, SimulacaoMoradiaService simulacaoService) {
        this.repository = repository;
        this.simulacaoService = simulacaoService;
    }

    public Integer proprietarioId(SimulacaoItem item) {
        return simulacaoService.proprietarioId(item.getSimulacao());
    }

    @Transactional
    public SimulacaoItem buscarDoUsuario(Integer id, Integer userId) {
        SimulacaoItem item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public SimulacaoItemResponse cadastrar(Integer simulacaoId, SimulacaoItemRequest request, Integer userId) {
        SimulacaoItem item = new SimulacaoItem();
        item.setSimulacao(simulacaoService.buscarDoUsuario(simulacaoId, userId));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public SimulacaoItemResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }


    @Transactional
    public List<SimulacaoItemResponse> listarPorSimulacao(Integer simulacaoId, Integer userId) {
        simulacaoService.buscarDoUsuario(simulacaoId, userId);
        return repository.findBySimulacao_IdOrderByIdAsc(simulacaoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, SimulacaoItemRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        SimulacaoItem item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(SimulacaoItem item, SimulacaoItemRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.categoria() != null) {
            if (request.categoria().isBlank() || request.categoria().strip().length() > 50) {
                throw new RegraNegocioException("Campo categoria inválido", HttpStatus.BAD_REQUEST);
            }
            item.setCategoria(request.categoria().strip());
        }
        if (item.getCategoria() == null) {
            throw new RegraNegocioException("Campo categoria obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.tipo() != null) {
            if (request.tipo().isBlank() || request.tipo().strip().length() > 30) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            if (!List.of("mensal", "inicial").contains(request.tipo())) {
                throw new RegraNegocioException("Campo tipo inválido", HttpStatus.BAD_REQUEST);
            }
            item.setTipo(request.tipo().strip());
        }
        if (item.getTipo() == null) {
            throw new RegraNegocioException("Campo tipo obrigatório", HttpStatus.BAD_REQUEST);
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
    }

    private SimulacaoItemResponse resposta(SimulacaoItem item) {
        return new SimulacaoItemResponse(
            item.getId(),
            item.getSimulacao().getId(),
            item.getNome(),
            item.getCategoria(),
            item.getTipo(),
            item.getValor()
        );
    }
}
