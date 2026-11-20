package com.iagora.backend.modules.financas.transacaoFinanceira;

import com.iagora.backend.modules.financas.categoriaFinancas.CategoriaFinancas;
import com.iagora.backend.modules.financas.contaFinanceira.ContaFinanceira;
import com.iagora.backend.modules.financas.recorrenciaFinanceira.RecorrenciaFinanceira;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "transacao_financeira")
public class TransacaoFinanceira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaFinancas categoria;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "conta_id")
    private ContaFinanceira conta;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)

    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "recorrencia_id")
    private RecorrenciaFinanceira recorrencia;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String descricao;

    @Column(name = "tipo", nullable = false, length = 30)
    private String tipo;

    @Column(name = "valor", nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Column(name = "competencia", nullable = false)
    private LocalDate competencia;

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Column(name = "pago_em")
    private LocalDate pagoEm;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "pendente";

    public TransacaoFinanceira() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public CategoriaFinancas getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaFinancas categoria) {
        this.categoria = categoria;
    }

    public ContaFinanceira getConta() {
        return conta;
    }

    public void setConta(ContaFinanceira conta) {
        this.conta = conta;
    }

    public RecorrenciaFinanceira getRecorrencia() {
        return recorrencia;
    }

    public void setRecorrencia(RecorrenciaFinanceira recorrencia) {
        this.recorrencia = recorrencia;
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

    public LocalDate getCompetencia() {
        return competencia;
    }

    public void setCompetencia(LocalDate competencia) {
        this.competencia = competencia;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public LocalDate getPagoEm() {
        return pagoEm;
    }

    public void setPagoEm(LocalDate pagoEm) {
        this.pagoEm = pagoEm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
