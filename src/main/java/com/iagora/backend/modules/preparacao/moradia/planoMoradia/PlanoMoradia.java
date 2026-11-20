package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejada;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "plano_moradia")
public class PlanoMoradia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "renda_mensal", nullable = false, precision = 12, scale = 2)
    private BigDecimal rendaMensal = BigDecimal.ZERO;

    @Column(name = "reserva_atual", nullable = false, precision = 12, scale = 2)
    private BigDecimal reservaAtual = BigDecimal.ZERO;

    @Column(name = "mudanca_prevista")
    private LocalDate mudancaPrevista;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "ativo";

    @ManyToOne(fetch = FetchType.LAZY, optional = true)

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "casa_desejada_id", unique = true)
    private CasaDesejada casaDesejada;

    public PlanoMoradia() {}

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

    public BigDecimal getRendaMensal() {
        return rendaMensal;
    }

    public void setRendaMensal(BigDecimal rendaMensal) {
        this.rendaMensal = rendaMensal;
    }

    public BigDecimal getReservaAtual() {
        return reservaAtual;
    }

    public void setReservaAtual(BigDecimal reservaAtual) {
        this.reservaAtual = reservaAtual;
    }

    public LocalDate getMudancaPrevista() {
        return mudancaPrevista;
    }

    public void setMudancaPrevista(LocalDate mudancaPrevista) {
        this.mudancaPrevista = mudancaPrevista;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public CasaDesejada getCasaDesejada() {
        return casaDesejada;
    }

    public void setCasaDesejada(CasaDesejada casaDesejada) {
        this.casaDesejada = casaDesejada;
    }
}
