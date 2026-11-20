package com.iagora.backend.modules.catalogo;

import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRepository;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRequest;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoService;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaRequest;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaService;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRequest;
import com.iagora.backend.modules.preparacao.aprendizado.alternativa.AlternativaRequest;
import com.iagora.backend.modules.preparacao.aprendizado.alternativa.AlternativaService;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaRequest;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaService;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoRequest;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoService;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizRequest;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@Order(1)
public class ImportadorCatalogo implements ApplicationRunner {
    private static final String VERSAO = "iagora-v1";
    private static final Logger LOG = LoggerFactory.getLogger(ImportadorCatalogo.class);

    private final ObjectMapper mapper;
    private final EntityManager entidade;
    private final ProdutoRepository produtosExistentes;
    private final ProdutoService produtos;
    private final ReceitaService receitas;
    private final AulaService aulas;
    private final QuizService quizzes;
    private final QuestaoService questoes;
    private final AlternativaService alternativas;
    private final boolean habilitado;

    public ImportadorCatalogo(ObjectMapper mapper, EntityManager entidade, ProdutoRepository produtosExistentes,
            ProdutoService produtos, ReceitaService receitas, AulaService aulas, QuizService quizzes,
            QuestaoService questoes, AlternativaService alternativas,
            @Value("${IAGORA_CATALOGO_IMPORTAR:false}") boolean habilitado) {
        this.mapper = mapper;
        this.entidade = entidade;
        this.produtosExistentes = produtosExistentes;
        this.produtos = produtos;
        this.receitas = receitas;
        this.aulas = aulas;
        this.quizzes = quizzes;
        this.questoes = questoes;
        this.alternativas = alternativas;
        this.habilitado = habilitado;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (habilitado) importar();
    }

    @Transactional
    public void importar() {
        if (entidade.find(CatalogoImportado.class, VERSAO) != null) return;
        var dados = ler();
        entidade.persist(new CatalogoImportado(VERSAO));
        entidade.flush();

        var ids = importarProdutos(dados.produtos());
        var nomes = receitas.listar().stream().filter(r -> r.userId() == null)
            .map(r -> textoNormalizado(r.nome())).collect(Collectors.toSet());
        for (var receita : dados.receitas()) {
            if (nomes.add(textoNormalizado(receita.nome()))) {
                var itens = receita.ingredientes().stream().map(i -> i.requisicao(ids)).toList();
                receitas.cadastrar(new ReceitaRequest(null, true, receita.nome(), receita.descricao(),
                    receita.tipo(), receita.preparo(), receita.imagem(), "publicada", itens));
            }
        }

        var titulos = aulas.listar().stream().map(a -> textoNormalizado(a.titulo())).collect(Collectors.toSet());
        for (var aula : dados.aulas()) {
            if (titulos.add(textoNormalizado(aula.titulo()))) importarAula(aula);
        }
        LOG.info("Catálogo {} processado: {} produtos, {} receitas e {} aulas",
            VERSAO, dados.produtos().size(), dados.receitas().size(), dados.aulas().size());
    }

    private Lote ler() {
        try (var entrada = new ClassPathResource("catalogo/" + VERSAO + ".json").getInputStream()) {
            return mapper.readValue(entrada, Lote.class);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível ler o catálogo " + VERSAO, ex);
        }
    }

    private Map<String, Integer> importarProdutos(List<ProdutoDados> dados) {
        var existentes = new HashMap<ChaveProduto, Integer>();
        produtosExistentes.findByUserIdIsNullOrderByIdAsc().forEach(p -> existentes.putIfAbsent(
            ChaveProduto.de(p.getNome(), p.getMarca(), p.getTamanho(), p.getUnidade()), p.getId()));
        var ids = new HashMap<String, Integer>();
        for (var p : dados) {
            if (p.ref() == null || p.ref().isBlank() || ids.containsKey(p.ref())) {
                throw new IllegalStateException("Referência de produto inválida ou duplicada: " + p.ref());
            }
            var chave = ChaveProduto.de(p.nome(), p.marca(), p.tamanho(), p.unidade());
            int id = existentes.computeIfAbsent(chave, ignorada -> produtos.cadastrar(
                new ProdutoRequest(p.nome(), p.marca(), p.tamanho(), p.unidade(), p.imagem())).id());
            ids.put(p.ref(), id);
        }
        return ids;
    }

    private void importarAula(AulaDados aula) {
        int aulaId = aulas.cadastrar(new AulaRequest(aula.titulo(), aula.descricao(), aula.conteudo(),
            aula.videoUrl(), aula.imagem(), aula.categoria())).id();
        for (var quiz : aula.quizzes()) {
            int quizId = quizzes.cadastrar(aulaId, new QuizRequest(quiz.titulo())).id();
            for (var questao : quiz.questoes()) {
                if (questao.alternativas().stream().filter(a -> Boolean.TRUE.equals(a.correta())).count() != 1) {
                    throw new IllegalStateException("A questão deve ter uma resposta correta: " + questao.enunciado());
                }
                int id = questoes.cadastrar(quizId, new QuestaoRequest(questao.enunciado())).id();
                questao.alternativas().forEach(a -> alternativas.cadastrar(id, a));
            }
        }
    }

    private static String textoNormalizado(String texto) {
        return texto == null ? "" : texto.strip().toLowerCase(Locale.ROOT);
    }

    private record ChaveProduto(String nome, String marca, BigDecimal tamanho, String unidade) {
        static ChaveProduto de(String nome, String marca, BigDecimal tamanho, String unidade) {
            return new ChaveProduto(textoNormalizado(nome), textoNormalizado(marca),
                tamanho == null ? null : tamanho.stripTrailingZeros(), textoNormalizado(unidade));
        }
    }

    record Lote(List<ProdutoDados> produtos, List<ReceitaDados> receitas, List<AulaDados> aulas) {}
    record ProdutoDados(String ref, String nome, String marca, BigDecimal tamanho, String unidade, String imagem) {}
    record ReceitaDados(String nome, String descricao, String tipo, String preparo, Boolean publica, Integer userId,
        String imagem, String statusPublicacao, List<IngredienteDados> ingredientes) {}
    record IngredienteDados(String produtoRef, String nomeItem, BigDecimal quantidade) {
        ReceitaItemRequest requisicao(Map<String, Integer> ids) {
            Integer produtoId = produtoRef == null ? null : ids.get(produtoRef);
            if (produtoRef != null && produtoId == null) {
                throw new IllegalStateException("Produto do ingrediente não encontrado: " + produtoRef);
            }
            return new ReceitaItemRequest(produtoId, nomeItem, quantidade);
        }
    }
    record AulaDados(String titulo, String descricao, String conteudo, String videoUrl, String imagem, String categoria,
        List<QuizDados> quizzes) {}
    record QuizDados(String titulo, List<QuestaoDados> questoes) {}
    record QuestaoDados(String enunciado, List<AlternativaRequest> alternativas) {}
}
