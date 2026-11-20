package com.iagora.backend.demo;

import com.iagora.backend.modules.casa.casa.CasaService;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasRequest;
import com.iagora.backend.modules.casa.cozinha.compras.ComprasService;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRequest;
import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemService;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemRequest;
import com.iagora.backend.modules.casa.cozinha.estoqueItem.EstoqueItemService;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoService;
import com.iagora.backend.modules.casa.cozinha.receita.ReceitaService;
import com.iagora.backend.modules.casa.cozinha.receitaFavorita.ReceitaFavoritaService;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoRequest;
import com.iagora.backend.modules.casa.limpeza.comodo.ComodoService;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaRequest;
import com.iagora.backend.modules.casa.limpeza.limpeza.LimpezaService;
import com.iagora.backend.modules.casa.tarefa.categoriaTarefa.CategoriaTarefaRequest;
import com.iagora.backend.modules.casa.tarefa.categoriaTarefa.CategoriaTarefaService;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefaRequest;
import com.iagora.backend.modules.casa.tarefa.execucaoTarefa.ExecucaoTarefaService;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaRequest;
import com.iagora.backend.modules.casa.tarefa.tarefa.TarefaService;
import com.iagora.backend.modules.financas.contaFinanceira.ContaFinanceiraRequest;
import com.iagora.backend.modules.financas.contaFinanceira.ContaFinanceiraService;
import com.iagora.backend.modules.identidade.autenticacao.AuthService;
import com.iagora.backend.modules.identidade.autenticacao.CadastroRequest;
import com.iagora.backend.modules.identidade.autenticacao.UserRepository;
import com.iagora.backend.modules.identidade.perfil.PerfilRequest;
import com.iagora.backend.modules.identidade.perfil.PerfilService;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaRequest;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaService;
import com.iagora.backend.modules.marketplace.servico.ServicoRequest;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaService;
import com.iagora.backend.modules.preparacao.aprendizado.progressoAula.ProgressoAulaService;
import com.iagora.backend.modules.preparacao.gastoPreparacao.GastoPreparacaoRequest;
import com.iagora.backend.modules.preparacao.gastoPreparacao.GastoPreparacaoService;
import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaRequest;
import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornadaService;
import com.iagora.backend.modules.preparacao.jornada.jornada.JornadaRequest;
import com.iagora.backend.modules.preparacao.jornada.jornada.JornadaService;
import com.iagora.backend.modules.preparacao.jornada.tarefaJornada.TarefaJornadaRequest;
import com.iagora.backend.modules.preparacao.jornada.tarefaJornada.TarefaJornadaService;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejadaRequest;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejadaService;
import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradiaRequest;
import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradiaService;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaRequest;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradiaService;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.function.IntConsumer;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("demo & !prod & !production")
public class ContasDemo {
    public static final String EMAIL_A = "moradia@demo.iagora.example";
    public static final String EMAIL_B = "planejamento@demo.iagora.example";
    public static final String EMAIL_C = "empresa@demo.iagora.example";

    private final UserRepository usuarios;
    private final AuthService autenticacao;
    private final PerfilService perfis;
    private final CasaService casas;
    private final ComodoService comodos;
    private final LimpezaService limpezas;
    private final ProdutoService produtos;
    private final EstoqueItemService estoque;
    private final ComprasService compras;
    private final CompraItemService itensCompra;
    private final ReceitaService receitas;
    private final ReceitaFavoritaService favoritas;
    private final CategoriaTarefaService categoriasTarefa;
    private final TarefaService tarefas;
    private final ExecucaoTarefaService execucoes;
    private final ContaFinanceiraService contasFinanceiras;
    private final CasaDesejadaService imoveis;
    private final PlanoMoradiaService planos;
    private final CustoMoradiaService custos;
    private final GastoPreparacaoService gastos;
    private final JornadaService jornadas;
    private final EtapaJornadaService etapas;
    private final TarefaJornadaService tarefasJornada;
    private final AulaService aulas;
    private final ProgressoAulaService progresso;
    private final PerfilEmpresaService empresas;
    private final ServicoService servicos;

    public ContasDemo(UserRepository usuarios, AuthService autenticacao, PerfilService perfis,
            CasaService casas, ComodoService comodos, LimpezaService limpezas, ProdutoService produtos,
            EstoqueItemService estoque, ComprasService compras, CompraItemService itensCompra,
            ReceitaService receitas, ReceitaFavoritaService favoritas, CategoriaTarefaService categoriasTarefa,
            TarefaService tarefas, ExecucaoTarefaService execucoes, ContaFinanceiraService contasFinanceiras, CasaDesejadaService imoveis,
            PlanoMoradiaService planos, CustoMoradiaService custos, GastoPreparacaoService gastos,
            JornadaService jornadas, EtapaJornadaService etapas, TarefaJornadaService tarefasJornada,
            AulaService aulas, ProgressoAulaService progresso, PerfilEmpresaService empresas, ServicoService servicos) {
        this.usuarios = usuarios;
        this.autenticacao = autenticacao;
        this.perfis = perfis;
        this.casas = casas;
        this.comodos = comodos;
        this.limpezas = limpezas;
        this.produtos = produtos;
        this.estoque = estoque;
        this.compras = compras;
        this.itensCompra = itensCompra;
        this.receitas = receitas;
        this.favoritas = favoritas;
        this.categoriasTarefa = categoriasTarefa;
        this.tarefas = tarefas;
        this.execucoes = execucoes;
        this.contasFinanceiras = contasFinanceiras;
        this.imoveis = imoveis;
        this.planos = planos;
        this.custos = custos;
        this.gastos = gastos;
        this.jornadas = jornadas;
        this.etapas = etapas;
        this.tarefasJornada = tarefasJornada;
        this.aulas = aulas;
        this.progresso = progresso;
        this.empresas = empresas;
        this.servicos = servicos;
    }

    public void popular(String senhaA, String senhaB, String senhaC) {
        criarConta(EMAIL_A, senhaA, 1, this::popularMoradia);
        criarConta(EMAIL_B, senhaB, 1, this::popularPlanejamento);
        criarConta(EMAIL_C, senhaC, 2, this::popularEmpresa);
    }

    private void criarConta(String email, String senha, int tipo, IntConsumer popular) {
        if (senha.isBlank() || usuarios.findByEmailIgnoreCase(email).isPresent()) return;
        if (senha.length() < 12 || senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalArgumentException("Use senhas demo com pelo menos 12 caracteres e até 72 bytes");
        }
        int id = autenticacao.cadastrar(new CadastroRequest(email, senha, tipo)).id();
        popular.accept(id);
    }

    private void popularMoradia(int usuario) {
        perfis.atualizar(perfis.buscarPorUserId(usuario).id(), new PerfilRequest("Júlia (demo)", null, 1, null));
        int casa = casas.buscarPorUserId(usuario).getFirst().id();
        int cozinha = comodos.cadastrar(new ComodoRequest(casa, "Cozinha", "cozinha", "Cozinha do apartamento")).id();
        comodos.cadastrar(new ComodoRequest(casa, "Quarto", "quarto", "Quarto principal"));
        limpezas.cadastrar(new LimpezaRequest(cozinha, "Higienizar a bancada", "Limpar após preparar as refeições", LocalDate.now()));
        var produto = produtos.buscarPorNome("Arroz integral").getFirst();
        estoque.cadastrar(new EstoqueItemRequest(casa, produto.id(), BigDecimal.ONE));
        int lista = compras.cadastrar(new ComprasRequest(casa, "Reposição da semana")).id();
        itensCompra.cadastrar(lista, new CompraItemRequest(null, "Banana prata", 6, null));
        receitas.listar().stream().filter(receita -> "Omelete de tomate para uma pessoa".equals(receita.nome()))
            .findFirst().ifPresent(receita -> favoritas.cadastrar(usuario, receita.id()));
        int categoria = categoriasTarefa.cadastrar(new CategoriaTarefaRequest("Casa", null, null, null), usuario).id();
        var tarefa = tarefas.cadastrar(new TarefaRequest(categoria, "Separar recicláveis", null, LocalDate.now().atTime(18, 0)), usuario);
        execucoes.cadastrar(tarefa.id(), new ExecucaoTarefaRequest(tarefa.prazo()), usuario);
        contasFinanceiras.cadastrar(new ContaFinanceiraRequest("Carteira", "corrente", BigDecimal.valueOf(350), null), usuario);
    }

    private void popularPlanejamento(int usuario) {
        perfis.atualizar(perfis.buscarPorUserId(usuario).id(), new PerfilRequest("Rafael (demo)", null, 2, null));
        int imovel = imoveis.cadastrar(new CasaDesejadaRequest(null, "Studio perto do trabalho",
            "Opção de um dormitório com acesso a transporte", "Apartamento", BigDecimal.valueOf(1250),
            "mensal", "sem-moveis", null, null), usuario).id();
        LocalDate mudanca = LocalDate.now().plusMonths(4);
        int plano = planos.cadastrar(new PlanoMoradiaRequest("Mudança em quatro meses",
            BigDecimal.valueOf(4200), BigDecimal.valueOf(2100), mudanca, imovel, null), usuario).id();
        custos.cadastrar(plano, new CustoMoradiaRequest("Frete", "transporte", "inicial", BigDecimal.valueOf(400)), usuario);
        gastos.cadastrar(new GastoPreparacaoRequest("Transporte mensal", BigDecimal.valueOf(180),
            "fixo", "continua", null, "transporte"), usuario);
        int jornada = jornadas.cadastrar(new JornadaRequest("Guia da primeira mudança", "Checklist guiado para preparar sua mudança e os primeiros dias na casa.", null), usuario).id();
        int etapa = etapas.cadastrar(jornada, new EtapaJornadaRequest("Pesquisar imóveis", "Comparar localização e custo",
            "moradia", 1, mudanca.minusWeeks(3), null), usuario).id();
        tarefasJornada.cadastrar(etapa, new TarefaJornadaRequest("Comparar três imóveis", null, 1), usuario);
        aulas.listar().stream().findFirst().ifPresent(aula -> progresso.concluir(aula.id(), usuario));
    }

    private void popularEmpresa(int usuario) {
        empresas.cadastrar(new PerfilEmpresaRequest(null, "Casa em Ordem (demo)", "00000000000000",
            "Empresa fictícia de organização doméstica e limpeza.", "limpeza", null, null, "(00) 00000-0000",
            null, null, EMAIL_C, null), usuario);
        empresas.enviarValidacao(usuario);
        servicos.cadastrar(new ServicoRequest(null, "Limpeza de apartamento compacto",
            "Organização e limpeza de apartamentos de até dois cômodos.", null, "limpeza",
            BigDecimal.valueOf(150), "fixo", null), usuario);
    }
}
