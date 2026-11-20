package com.iagora.backend.modules.casa.cozinha.receitaItem;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.iagora.backend.modules.casa.cozinha.produto.Produto;
import com.iagora.backend.modules.casa.cozinha.receita.Receita;

@Entity
@Table(name = "receita_item")
public class ReceitaItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "nome_item")
    private String nomeItem;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade;

    public ReceitaItem() {}

    public Integer getId() {
        return id;
    }

    public Receita getReceita() {
        return receita;
    }

    public void setReceita(Receita receita) {
        this.receita = receita;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public String getNomeItem() {
        return nomeItem;
    }

    public void setNomeItem(String nomeItem) {
        this.nomeItem = nomeItem;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}
