package com.iagora.backend.modules.comercial.metodoPagamento;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "metodo_pagamento")
public class MetodoPagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "apelido", length = 100)
    private String apelido;

    @Column(name = "bandeira", length = 30)
    private String bandeira;

    @Column(name = "ultimos_digitos", length = 4)
    private String ultimosDigitos;

    @Column(name = "token_gateway", nullable = false, unique = true, length = 255)
    private String tokenGateway;

    @Column(name = "validade_mes")
    private Integer validadeMes;

    @Column(name = "validade_ano")
    private Integer validadeAno;

    @Column(name = "principal", nullable = false)
    private Boolean principal = false;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    public MetodoPagamento() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getApelido() {
        return apelido;
    }

    public void setApelido(String apelido) {
        this.apelido = apelido;
    }

    public String getBandeira() {
        return bandeira;
    }

    public void setBandeira(String bandeira) {
        this.bandeira = bandeira;
    }

    public String getUltimosDigitos() {
        return ultimosDigitos;
    }

    public void setUltimosDigitos(String ultimosDigitos) {
        this.ultimosDigitos = ultimosDigitos;
    }

    public String getTokenGateway() {
        return tokenGateway;
    }

    public void setTokenGateway(String tokenGateway) {
        this.tokenGateway = tokenGateway;
    }

    public Integer getValidadeMes() {
        return validadeMes;
    }

    public void setValidadeMes(Integer validadeMes) {
        this.validadeMes = validadeMes;
    }

    public Integer getValidadeAno() {
        return validadeAno;
    }

    public void setValidadeAno(Integer validadeAno) {
        this.validadeAno = validadeAno;
    }

    public Boolean getPrincipal() {
        return principal;
    }

    public void setPrincipal(Boolean principal) {
        this.principal = principal;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
