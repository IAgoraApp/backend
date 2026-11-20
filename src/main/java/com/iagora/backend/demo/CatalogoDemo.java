package com.iagora.backend.demo;

import com.iagora.backend.modules.comercial.planoAssinatura.PlanoAssinaturaRequest;
import com.iagora.backend.modules.comercial.planoAssinatura.PlanoAssinaturaService;
import com.iagora.backend.modules.comercial.planoPublicacao.PlanoPublicacaoRequest;
import com.iagora.backend.modules.comercial.planoPublicacao.PlanoPublicacaoService;
import com.iagora.backend.modules.preparacao.aprendizado.alternativa.AlternativaRequest;
import com.iagora.backend.modules.preparacao.aprendizado.alternativa.AlternativaService;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaRequest;
import com.iagora.backend.modules.preparacao.aprendizado.aula.AulaService;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoRequest;
import com.iagora.backend.modules.preparacao.aprendizado.questao.QuestaoService;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizRequest;
import com.iagora.backend.modules.preparacao.aprendizado.quiz.QuizService;
import com.iagora.backend.modules.casa.cozinha.receita.CatalogoInicialReceitas;
import java.math.BigDecimal;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@Profile("demo & !prod & !production")
public class CatalogoDemo {
    private final CatalogoInicialReceitas catalogo;
    private final AulaService aulas;
    private final QuizService quizzes;
    private final QuestaoService questoes;
    private final AlternativaService alternativas;
    private final PlanoPublicacaoService publicacoes;
    private final PlanoAssinaturaService assinaturas;

    public CatalogoDemo(CatalogoInicialReceitas catalogo,
            AulaService aulas, QuizService quizzes, QuestaoService questoes, AlternativaService alternativas,
            PlanoPublicacaoService publicacoes, PlanoAssinaturaService assinaturas) {
        this.catalogo = catalogo;
        this.aulas = aulas;
        this.quizzes = quizzes;
        this.questoes = questoes;
        this.alternativas = alternativas;
        this.publicacoes = publicacoes;
        this.assinaturas = assinaturas;
    }

    public void popular() {
        catalogo.produto("Detergente neutro", 500, "ml");
        catalogo.produto("Sabão líquido para roupas", 1000, "ml");
        catalogo.produto("Sabonete neutro", 90, "g");

        aula("Organize a primeira semana na casa", "moradia",
            "Liste o que precisa funcionar no primeiro dia. Separe ferramentas, roupa de cama e itens de higiene.",
            "O que vale separar antes da mudança?", "Itens essenciais para o primeiro dia",
            "Toda a decoração", "Somente objetos de lazer");
        aula("Planeje refeições sem desperdício", "culinaria",
            "Confira a despensa. Escolha refeições que compartilhem ingredientes. Separe porções antes de congelar.",
            "O que consultar antes de montar a lista do mercado?", "A despensa",
            "A cor das embalagens", "A ordem das prateleiras");
        aula("Conheça seus gastos mensais", "financas",
            "Some renda líquida e despesas recorrentes. Reserve uma margem para gastos variáveis e imprevistos.",
            "Qual despesa entra no orçamento de todo mês?", "A conta mensal de internet",
            "A caução de entrada", "A compra inicial de um sofá");
        aula("Faça um roteiro de limpeza", "limpeza",
            "Divida a casa por cômodos e planeje pequenos blocos. Leia o rótulo e use cada produto conforme indicado.",
            "Como escolher um produto de limpeza?", "Leia o rótulo e respeite a indicação de uso",
            "Pela cor da embalagem", "Pelo perfume mais forte");
        aula("Como se preparar para a vistoria", "moradia",
            "Leve um checklist, confira portas, iluminação e torneiras e registre o estado do imóvel por escrito.",
            "O que ajuda a registrar a condição do imóvel?", "Checklist e fotos da vistoria",
            "Somente a decoração", "A lista do supermercado");
        aula("Saiba onde desligar água e energia", "manutencao",
            "Identifique os registros de água e o disjuntor principal no primeiro dia. Mantenha esses pontos acessíveis.",
            "Qual ponto deve ficar acessível?", "O quadro de disjuntores",
            "O armário de decoração", "A caixa de sapatos");

        if (publicacoes.listar().stream().noneMatch(plano -> plano.nome().equals("Vitrine demo 30 dias"))) {
            publicacoes.cadastrar(new PlanoPublicacaoRequest("Vitrine demo 30 dias",
                "Plano fictício para demonstração de anúncios.", new BigDecimal("29.90"), 30));
        }
        if (assinaturas.listar().stream().noneMatch(plano -> plano.nome().equals("Organização demo"))) {
            assinaturas.cadastrar(new PlanoAssinaturaRequest("Organização demo",
                "Plano fictício de exemplo para organizar a casa.", new BigDecimal("14.90"), null));
        }
    }

    private void aula(String titulo, String categoria, String conteudo, String pergunta,
            String correta, String primeiraDistratora, String segundaDistratora) {
        if (aulas.listar().stream().anyMatch(aula -> aula.titulo().equals(titulo))) return;
        int aula = aulas.cadastrar(new AulaRequest(titulo, conteudo, conteudo, null, null, categoria)).id();
        int quiz = quizzes.cadastrar(aula, new QuizRequest("Revise o que aprendeu")).id();
        int questao = questoes.cadastrar(quiz, new QuestaoRequest(pergunta)).id();
        alternativas.cadastrar(questao, new AlternativaRequest(primeiraDistratora, false));
        alternativas.cadastrar(questao, new AlternativaRequest(correta, true));
        alternativas.cadastrar(questao, new AlternativaRequest(segundaDistratora, false));
    }
}
