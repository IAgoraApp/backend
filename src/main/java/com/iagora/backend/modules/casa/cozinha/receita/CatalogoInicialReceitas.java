package com.iagora.backend.modules.casa.cozinha.receita;

import com.iagora.backend.modules.casa.cozinha.produto.ProdutoRequest;
import com.iagora.backend.modules.casa.cozinha.produto.ProdutoService;
import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRequest;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(0)
public class CatalogoInicialReceitas implements ApplicationRunner {
    private final ProdutoService produtos;
    private final ReceitaService receitas;

    public CatalogoInicialReceitas(ProdutoService produtos, ReceitaService receitas) {
        this.produtos = produtos;
        this.receitas = receitas;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments argumentos) {
        int arroz = produto("Arroz integral", 1000, "g");
        int grao = produto("Grão-de-bico seco", 500, "g");
        int ovo = produto("Ovos brancos", 6, "un");
        int tomate = produto("Tomate italiano", 1, "un");
        int aveia = produto("Aveia em flocos", 170, "g");
        int banana = produto("Banana prata", 1, "un");
        receita("Arroz integral com tomate", "Um almoço econômico que rende duas porções.",
            "almoco", "Cozinhe o arroz. Pique o tomate e refogue até amolecer. Misture e tempere a gosto.",
            List.of(item(arroz, "Arroz integral", 160), item(tomate, "Tomate italiano", 2)));
        receita("Omelete de tomate para uma pessoa", "Jantar de frigideira com dois ingredientes.",
            "jantar", "Bata os ovos. Refogue o tomate picado. Junte os ovos e cozinhe até firmar.",
            List.of(item(ovo, "Ovos brancos", 2), item(tomate, "Tomate italiano", 1)));
        receita("Aveia com banana na panela", "Café da manhã simples para os dias de pressa.",
            "cafe_da_manha", "Cozinhe a aveia em água por cinco minutos. Amasse a banana e misture antes de servir.",
            List.of(item(aveia, "Aveia em flocos", 35), item(banana, "Banana prata", 1)));
        receita("Salada morna de grão-de-bico", "Porção para o almoço com ingredientes de despensa.",
            "almoco", "Deixe o grão de molho, descarte a água e cozinhe até ficar macio. Junte o tomate e tempere.",
            List.of(item(grao, "Grão-de-bico seco", 80), item(tomate, "Tomate italiano", 1)));
    }

    public int produto(String nome, int tamanho, String unidade) {
        BigDecimal quantidade = BigDecimal.valueOf(tamanho);
        return produtos.buscarPorNome(nome).stream()
            .filter(produto -> nome.equalsIgnoreCase(produto.nome()) && unidade.equals(produto.unidade())
                && produto.tamanho() != null && quantidade.compareTo(produto.tamanho()) == 0
                && (produto.marca() == null || produto.marca().isBlank()))
            .findFirst().orElseGet(() -> produtos.cadastrar(new ProdutoRequest(nome, null, quantidade, unidade, null))).id();
    }

    private ReceitaItemRequest item(int produto, String nome, int quantidade) {
        return new ReceitaItemRequest(produto, nome, BigDecimal.valueOf(quantidade));
    }

    private void receita(String nome, String descricao, String tipo, String preparo, List<ReceitaItemRequest> itens) {
        if (receitas.listar().stream().anyMatch(receita -> receita.userId() == null && receita.nome().equals(nome))) return;
        receitas.cadastrar(new ReceitaRequest(null, true, nome, descricao, tipo, preparo, null, null, itens));
    }
}
