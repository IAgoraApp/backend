package com.iagora.backend.modules.casa.cozinha.estoqueItem;

import jakarta.persistence.*;
import java.math.BigDecimal;

import com.iagora.backend.modules.casa.casa.Casa;
import com.iagora.backend.modules.casa.cozinha.produto.Produto;

@Entity
@Table(name = "estoque_item", uniqueConstraints = @UniqueConstraint(columnNames = { "casa_id", "produto_id" }))
public class EstoqueItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    private Produto produto;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantidade = BigDecimal.ZERO;

    public EstoqueItem() {}

    public Integer getId() {
        return id;
    }

    public Casa getCasa() {
        return casa;
    }

    public void setCasa(Casa casa) {
        this.casa = casa;
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }
}
