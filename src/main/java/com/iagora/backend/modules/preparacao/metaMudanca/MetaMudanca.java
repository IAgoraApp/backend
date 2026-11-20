package com.iagora.backend.modules.preparacao.metaMudanca;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "meta_mudanca")
public class MetaMudanca {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "fonte", nullable = false, length = 20)
    private String fonte;

    @Column(name = "alvo", nullable = false, precision = 12, scale = 2)
    private BigDecimal alvo;

    @Column(name = "modo", nullable = false, length = 20)
    private String modo;

    @Column(name = "data")
    private LocalDate data;

    @Column(name = "aporte", precision = 12, scale = 2)
    private BigDecimal aporte;

    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "estimativa_mudanca")
    private JsonNode estimativaMudanca;

    public MetaMudanca() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getFonte() {
        return fonte;
    }

    public void setFonte(String fonte) {
        this.fonte = fonte;
    }

    public BigDecimal getAlvo() {
        return alvo;
    }

    public void setAlvo(BigDecimal alvo) {
        this.alvo = alvo;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public BigDecimal getAporte() {
        return aporte;
    }

    public void setAporte(BigDecimal aporte) {
        this.aporte = aporte;
    }

    public JsonNode getEstimativaMudanca() {
        return estimativaMudanca;
    }

    public void setEstimativaMudanca(JsonNode estimativaMudanca) {
        this.estimativaMudanca = estimativaMudanca;
    }
}
