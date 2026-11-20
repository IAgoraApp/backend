package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaService;
import com.iagora.backend.modules.preparacao.moradia.simulacaoItem.SimulacaoItem;
import com.iagora.backend.modules.preparacao.moradia.simulacaoItem.SimulacaoItemRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SimulacaoMoradiaService {
    private final SimulacaoItemRepository itemRepository;
    private final SimulacaoMoradiaRepository repository;
    private final PlanoMoradiaService planoService;

    public SimulacaoMoradiaService(SimulacaoItemRepository itemRepository, SimulacaoMoradiaRepository repository,
                                   PlanoMoradiaService planoService) {
        this.itemRepository = itemRepository;
        this.repository = repository;
        this.planoService = planoService;
    }

    public Integer proprietarioId(SimulacaoMoradia item) {
        return planoService.proprietarioId(item.getPlano());
    }

    @Transactional
    public SimulacaoMoradia buscarDoUsuario(Integer id, Integer userId) {
        SimulacaoMoradia item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public SimulacaoMoradiaResponse cadastrar(Integer planoId, SimulacaoMoradiaRequest request, Integer userId) {
        SimulacaoMoradia item = new SimulacaoMoradia();
        item.setPlano(planoService.buscarDoUsuario(planoId, userId));
        item.setCriadaEm(LocalDateTime.now());
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public SimulacaoMoradiaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }


    @Transactional
    public List<SimulacaoMoradiaResponse> listarPorPlano(Integer planoId, Integer userId) {
        planoService.buscarDoUsuario(planoId, userId);
        return repository.findByPlano_IdOrderByIdAsc(planoId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, SimulacaoMoradiaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        SimulacaoMoradia item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(SimulacaoMoradia item, SimulacaoMoradiaRequest request, Integer userId) {
        if (request.nome() != null) {
            if (request.nome().isBlank() || request.nome().strip().length() > 150) {
                throw new RegraNegocioException("Campo nome inválido", HttpStatus.BAD_REQUEST);
            }
            item.setNome(request.nome().strip());
        }
        if (item.getNome() == null) {
            throw new RegraNegocioException("Campo nome obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.rendaMensal() != null) {
            if (request.rendaMensal().scale() > 2 || request.rendaMensal().precision() - request.rendaMensal().scale() > 10 || request.rendaMensal().signum() < 0) {
                throw new RegraNegocioException("Campo rendaMensal inválido", HttpStatus.BAD_REQUEST);
            }
            item.setRendaMensal(request.rendaMensal());
        }
        if (item.getRendaMensal() == null) {
            throw new RegraNegocioException("Campo rendaMensal obrigatório", HttpStatus.BAD_REQUEST);
        }
        if (request.reserva() != null) {
            if (request.reserva().scale() > 2 || request.reserva().precision() - request.reserva().scale() > 10 || request.reserva().signum() < 0) {
                throw new RegraNegocioException("Campo reserva inválido", HttpStatus.BAD_REQUEST);
            }
            item.setReserva(request.reserva());
        }
    }

    private SimulacaoMoradiaResponse resposta(SimulacaoMoradia item) {
        return new SimulacaoMoradiaResponse(
            item.getId(),
            item.getPlano().getId(),
            item.getNome(),
            item.getRendaMensal(),
            item.getReserva(),
            item.getCriadaEm()
        );
    }

    @Transactional
    public ResumoSimulacaoMoradiaResponse resumo(Integer id, Integer userId) {
        var simulacao = buscarDoUsuario(id, userId);
        var itens = itemRepository.findBySimulacao_IdOrderByIdAsc(id);
        BigDecimal mensais = total(itens, "mensal");
        BigDecimal iniciais = total(itens, "inicial");
        return new ResumoSimulacaoMoradiaResponse(simulacao.getRendaMensal(), simulacao.getReserva(), mensais,
            iniciais, simulacao.getRendaMensal().subtract(mensais), simulacao.getReserva().subtract(iniciais));
    }

    private BigDecimal total(List<SimulacaoItem> itens, String tipo) {
        return itens.stream().filter(item -> tipo.equals(item.getTipo()))
            .map(SimulacaoItem::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
