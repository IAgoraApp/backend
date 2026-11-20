package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "casa_desejada")
public class CasaDesejada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "endereco_id")
    private Integer enderecoId;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descricao", length = 500)
    private String descricao;

    @Column(name = "tipo", nullable = false, length = 150)
    private String tipo;

    @Column(name = "valor", precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "valor_tipo", length = 20)
    private String valorTipo;

    @Column(name = "mobilia", length = 30)
    private String mobilia;

    public CasaDesejada() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getEnderecoId() {
        return enderecoId;
    }

    public void setEnderecoId(Integer enderecoId) {
        this.enderecoId = enderecoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public String getValorTipo() {
        return valorTipo;
    }

    public void setValorTipo(String valorTipo) {
        this.valorTipo = valorTipo;
    }

    public String getMobilia() {
        return mobilia;
    }

    public void setMobilia(String mobilia) {
        this.mobilia = mobilia;
    }
}
