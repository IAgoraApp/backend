package com.iagora.backend.modules.casa.cozinha.compraItem;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.iagora.backend.modules.casa.cozinha.compras.Compras;
import com.iagora.backend.modules.casa.cozinha.produto.Produto;

@Entity
@Table(name = "compra_item")
public class CompraItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "compras_id", nullable = false)
    private Compras compras;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "produto_id")
    private Produto produto;

    @Column(name = "nome_item")
    private String nomeItem;

    @Column(nullable = false)
    private Integer quantidade = 1;

    @Column(nullable = false)
    private Boolean comprado = false;

    @Column(precision = 10, scale = 2)
    private BigDecimal valor;

    public CompraItem() {}

    public Integer getId() {
        return id;
    }

    public Compras getCompras() {
        return compras;
    }

    public void setCompras(Compras compras) {
        this.compras = compras;
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

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Boolean getComprado() {
        return comprado;
    }

    public void setComprado(Boolean comprado) {
        this.comprado = comprado;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
