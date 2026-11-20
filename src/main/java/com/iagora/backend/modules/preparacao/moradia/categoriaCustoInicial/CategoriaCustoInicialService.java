package com.iagora.backend.modules.preparacao.moradia.categoriaCustoInicial;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejada;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejadaService;
import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradiaRepository;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradia;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaRepository;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class CategoriaCustoInicialService {

    private static final Map<String, List<String>> PADRAO = Map.of(
        "moradia", List.of("moradia", "documentacao"),
        "reformas", List.of("reformas", "manutencao", "servicos"),
        "mobilia", List.of("mobilia", "eletrodomesticos", "organizacao"),
        "transporte", List.of("transporte", "mudanca", "limpeza", "outros")
    );

    private final CategoriaCustoInicialRepository repository;
    private final PlanoMoradiaRepository planoRepository;
    private final CustoMoradiaRepository custoRepository;
    private final PlanoMoradiaService planoService;
    private final CasaDesejadaService casaService;
    private final EntityManager entityManager;

    public CategoriaCustoInicialService(CategoriaCustoInicialRepository repository, PlanoMoradiaRepository planoRepository,
                                        CustoMoradiaRepository custoRepository, PlanoMoradiaService planoService,
                                        CasaDesejadaService casaService, EntityManager entityManager) {
        this.repository = repository;
        this.planoRepository = planoRepository;
        this.custoRepository = custoRepository;
        this.planoService = planoService;
        this.casaService = casaService;
        this.entityManager = entityManager;
    }

    @Transactional
    public List<CategoriaCustoInicialResponse> listar(Integer userId, Integer planoId, Integer casaDesejadaId) {
        return consultar(escopo(userId, planoId, casaDesejadaId)).stream().map(this::resposta).toList();
    }

    @Transactional
    public CategoriaCustoInicialResponse cadastrar(Integer userId, CategoriaCustoInicialRequest request) {
        Escopo escopo = escopo(userId, request.planoId(), request.casaDesejadaId());
        entityManager.lock(escopo.user(), LockModeType.PESSIMISTIC_WRITE);
        String categoria = validar(request.categoria(), 50);
        String titulo = validar(request.titulo(), 60);
        String foto = request.foto() == null ? null : validar(request.foto(), 150);
        if (PADRAO.containsKey(categoria) || consultar(escopo).stream().anyMatch(item -> categoria.equals(item.getCategoria()))) {
            throw new RegraNegocioException("Categoria já existente", HttpStatus.CONFLICT);
        }
        var item = nova(escopo, categoria);
        item.setTitulo(titulo);
        item.setFoto(foto);
        return resposta(repository.save(item));
    }

    @Transactional
    public void apagar(Integer userId, Integer planoId, Integer casaDesejadaId, String categoria) {
        Escopo escopo = escopo(userId, planoId, casaDesejadaId);
        entityManager.lock(escopo.user(), LockModeType.PESSIMISTIC_WRITE);
        validar(categoria, 50);
        var existente = consultar(escopo).stream().filter(item -> categoria.equals(item.getCategoria())).findFirst();
        if (PADRAO.containsKey(categoria)) {
            var item = existente.orElseGet(() -> nova(escopo, categoria));
            item.setOculta(true);
            repository.save(item);
        } else {
            repository.delete(existente.orElseThrow(() ->
                new RegraNegocioException("Categoria não encontrada", HttpStatus.NOT_FOUND)));
        }
        if (escopo.plano() != null) {
            custoRepository.deleteByPlano_IdAndTipoAndCategoriaIn(
                escopo.plano().getId(), "inicial", PADRAO.getOrDefault(categoria, List.of(categoria)));
        }
    }

    private List<CategoriaCustoInicial> consultar(Escopo escopo) {
        if (escopo.casa() != null) {
            return repository.findByUser_IdAndCasaDesejada_IdOrderByIdAsc(escopo.user().getId(), escopo.casa().getId());
        }
        return repository.findByUser_IdAndPlano_IdOrderByIdAsc(escopo.user().getId(), escopo.plano().getId());
    }

    private Escopo escopo(Integer userId, Integer planoId, Integer casaDesejadaId) {
        if ((planoId == null) == (casaDesejadaId == null)) {
            throw new RegraNegocioException("Informe um plano ou um imóvel", HttpStatus.BAD_REQUEST);
        }
        if (planoId != null) {
            PlanoMoradia plano = planoService.buscarDoUsuario(planoId, userId);
            return new Escopo(plano.getUser(), plano, plano.getCasaDesejada());
        }
        CasaDesejada casa = casaService.buscarDoUsuario(casaDesejadaId, userId);
        return new Escopo(casa.getUser(), planoRepository.findByCasaDesejada_Id(casaDesejadaId).orElse(null), casa);
    }

    private CategoriaCustoInicial nova(Escopo escopo, String categoria) {
        var item = new CategoriaCustoInicial();
        item.setUser(escopo.user());
        if (escopo.casa() == null) {
            item.setPlano(escopo.plano());
        }
        item.setCasaDesejada(escopo.casa());
        item.setCategoria(categoria);
        return item;
    }

    private CategoriaCustoInicialResponse resposta(CategoriaCustoInicial item) {
        return new CategoriaCustoInicialResponse(item.getId(),
            item.getPlano() == null ? null : item.getPlano().getId(),
            item.getCasaDesejada() == null ? null : item.getCasaDesejada().getId(),
            item.getCategoria(), item.getTitulo(), item.getFoto(), item.getOculta());
    }

    private String validar(String valor, int limite) {
        if (valor == null || valor.isBlank() || valor.strip().length() > limite) {
            throw new RegraNegocioException("Campo da categoria inválido", HttpStatus.BAD_REQUEST);
        }
        return valor.strip();
    }

    private record Escopo(User user, PlanoMoradia plano, CasaDesejada casa) {}
}
