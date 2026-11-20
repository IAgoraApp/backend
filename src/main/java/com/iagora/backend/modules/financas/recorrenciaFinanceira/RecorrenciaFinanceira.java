package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "recorrencia_financeira")
public class RecorrenciaFinanceira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "valor_previsto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorPrevisto;

    @Column(name = "frequencia", nullable = false, length = 30)
    private String frequencia;

    @Column(name = "inicio_em", nullable = false)
    private LocalDate inicioEm;

    @Column(name = "fim_em")
    private LocalDate fimEm;

    @Column(name = "ativa", nullable = false)
    private Boolean ativa = true;

    public RecorrenciaFinanceira() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public BigDecimal getValorPrevisto() {
        return valorPrevisto;
    }

    public void setValorPrevisto(BigDecimal valorPrevisto) {
        this.valorPrevisto = valorPrevisto;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public LocalDate getInicioEm() {
        return inicioEm;
    }

    public void setInicioEm(LocalDate inicioEm) {
        this.inicioEm = inicioEm;
    }

    public LocalDate getFimEm() {
        return fimEm;
    }

    public void setFimEm(LocalDate fimEm) {
        this.fimEm = fimEm;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    @Column(name = "processada_ate")
    private LocalDate processadaAte;

    public LocalDate getProcessadaAte() {
        return processadaAte;
    }

    public void setProcessadaAte(LocalDate processadaAte) {
        this.processadaAte = processadaAte;
    }
}
