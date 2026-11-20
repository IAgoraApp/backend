package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradia;
import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "custo_moradia")
public class CustoMoradia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoMoradia plano;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "categoria", nullable = false, length = 50)
    private String categoria;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor = BigDecimal.ZERO;

    public CustoMoradia() {}

    public Integer getId() {
        return id;
    }

    public PlanoMoradia getPlano() {
        return plano;
    }

    public void setPlano(PlanoMoradia plano) {
        this.plano = plano;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }
}
