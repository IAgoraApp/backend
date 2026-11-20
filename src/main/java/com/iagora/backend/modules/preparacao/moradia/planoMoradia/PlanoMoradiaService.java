package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.preparacao.dinheiroPreparacao.DinheiroPreparacaoService;
import com.iagora.backend.modules.preparacao.gastoPreparacao.GastoPreparacao;
import com.iagora.backend.modules.preparacao.gastoPreparacao.GastoPreparacaoRepository;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejadaService;
import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradia;
import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradiaRepository;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class PlanoMoradiaService {
    private final DinheiroPreparacaoService dinheiroService;
    private final GastoPreparacaoRepository gastoRepository;
    private final CustoMoradiaRepository custoRepository;
    private final PlanoMoradiaRepository repository;
    private final UserRepository userRepository;
    private final CasaDesejadaService casaDesejadaService;

    public PlanoMoradiaService(DinheiroPreparacaoService dinheiroService, GastoPreparacaoRepository gastoRepository,
                               CustoMoradiaRepository custoRepository, PlanoMoradiaRepository repository,
                               UserRepository userRepository, CasaDesejadaService casaDesejadaService) {
        this.dinheiroService = dinheiroService;
        this.gastoRepository = gastoRepository;
        this.custoRepository = custoRepository;
        this.repository = repository;
        this.userRepository = userRepository;
        this.casaDesejadaService = casaDesejadaService;
    }

    public Integer proprietarioId(PlanoMoradia item) {
        return item.getUser().getId();
    }

    @Transactional
    public PlanoMoradia buscarDoUsuario(Integer id, Integer userId) {
        PlanoMoradia item = repository.findById(id).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND));
        if (!proprietarioId(item).equals(userId)) {
            throw new RegraNegocioException("Você não tem permissão para acessar este recurso", HttpStatus.FORBIDDEN);
        }
        return item;
    }

    @Transactional
    public PlanoMoradiaResponse cadastrar(PlanoMoradiaRequest request, Integer userId) {
        PlanoMoradia item = new PlanoMoradia();
        if (request.userId() != null && !request.userId().equals(userId)) {
            throw new RegraNegocioException("Usuário inválido para esta operação", HttpStatus.FORBIDDEN);
        }
        item.setUser(userRepository.findById(userId).orElseThrow(() -> new RegraNegocioException("Registro não encontrado", HttpStatus.NOT_FOUND)));
        aplicar(item, request, userId);
        return resposta(repository.save(item));
    }

    @Transactional
    public PlanoMoradiaResponse buscarPorId(Integer id, Integer userId) {
        return resposta(buscarDoUsuario(id, userId));
    }

    @Transactional
    public List<PlanoMoradiaResponse> listarPorUsuario(Integer userId) {
        return repository.findByUser_IdOrderByIdAsc(userId).stream().map(this::resposta).toList();
    }

    @Transactional
    public void atualizar(Integer id, PlanoMoradiaRequest request, Integer userId) {
        aplicar(buscarDoUsuario(id, userId), request, userId);
    }

    @Transactional
    public void apagar(Integer id, Integer userId) {
        PlanoMoradia item = buscarDoUsuario(id, userId);
        repository.delete(item);
    }

    private void aplicar(PlanoMoradia item, PlanoMoradiaRequest request, Integer userId) {
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
        if (request.reservaAtual() != null) {
            if (request.reservaAtual().scale() > 2 || request.reservaAtual().precision() - request.reservaAtual().scale() > 10 || request.reservaAtual().signum() < 0) {
                throw new RegraNegocioException("Campo reservaAtual inválido", HttpStatus.BAD_REQUEST);
            }
            item.setReservaAtual(request.reservaAtual());
        }
        if (request.mudancaPrevista() != null) {
            item.setMudancaPrevista(request.mudancaPrevista());
        }
        if (request.casaDesejadaId() != null) {
            item.setCasaDesejada(casaDesejadaService.buscarDoUsuario(request.casaDesejadaId(), userId));
        }
    }

    private PlanoMoradiaResponse resposta(PlanoMoradia item) {
        return new PlanoMoradiaResponse(
            item.getId(),
            item.getUser().getId(),
            item.getNome(),
            item.getRendaMensal(),
            item.getReservaAtual(),
            item.getMudancaPrevista(),
            item.getStatus(),
            item.getCasaDesejada() == null ? null : item.getCasaDesejada().getId()
        );
    }

    @Transactional
    public void concluir(Integer id, Integer userId) {
        PlanoMoradia item = buscarDoUsuario(id, userId);
        item.setStatus("concluido");
    }

    @Transactional
    public void arquivar(Integer id, Integer userId) {
        PlanoMoradia item = buscarDoUsuario(id, userId);
        item.setStatus("arquivado");
    }

    @Transactional
    public void reativar(Integer id, Integer userId) {
        PlanoMoradia item = buscarDoUsuario(id, userId);
        item.setStatus("ativo");
    }

    @Transactional
    public ResumoPlanoMoradiaResponse resumo(Integer id, Integer userId) {
        PlanoMoradia plano = buscarDoUsuario(id, userId);
        var dinheiro = dinheiroService.consultar(userId);
        BigDecimal renda = Objects.requireNonNullElse(dinheiro.renda(), plano.getRendaMensal());
        BigDecimal reserva = Objects.requireNonNullElse(dinheiro.reserva(), plano.getReservaAtual());
        var mudancaPrevista = dinheiro.mudancaPrevista() == null ? plano.getMudancaPrevista() : dinheiro.mudancaPrevista();
        var custos = custoRepository.findByPlano_IdOrderByIdAsc(id);
        BigDecimal mensais = total(custos, "mensal");
        BigDecimal iniciais = total(custos, "inicial");
        var imovel = plano.getCasaDesejada();
        if (imovel != null && imovel.getValor() != null && "mensal".equals(imovel.getValorTipo())) {
            BigDecimal aluguelPrevisto = custos.stream()
                .filter(item -> "mensal".equals(item.getTipo()) && "moradia".equals(item.getCategoria())
                    && List.of("Aluguel", "Aluguel ou prestação").contains(item.getNome()))
                .map(CustoMoradia::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            mensais = mensais.subtract(aluguelPrevisto).add(imovel.getValor());
        }
        BigDecimal mantidos = gastoRepository.findByUser_IdOrderByIdAsc(userId).stream()
            .filter(item -> "continua".equals(item.getDestino()))
            .map(GastoPreparacao::getValor)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal futuros = mensais.add(mantidos);
        return new ResumoPlanoMoradiaResponse(renda, reserva, mudancaPrevista, mensais, iniciais, mantidos,
            futuros, renda.subtract(futuros), reserva.subtract(iniciais));
    }

    private BigDecimal total(List<CustoMoradia> custos, String tipo) {
        return custos.stream().filter(item -> tipo.equals(item.getTipo()))
            .map(CustoMoradia::getValor).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
