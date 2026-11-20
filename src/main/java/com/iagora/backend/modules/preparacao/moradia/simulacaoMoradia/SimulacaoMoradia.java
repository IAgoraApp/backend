package com.iagora.backend.modules.preparacao.moradia.simulacaoMoradia;

import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradia;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "simulacao_moradia")
public class SimulacaoMoradia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoMoradia plano;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "renda_mensal", nullable = false, precision = 12, scale = 2)
    private BigDecimal rendaMensal;

    @Column(name = "reserva", nullable = false, precision = 12, scale = 2)
    private BigDecimal reserva = BigDecimal.ZERO;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    public SimulacaoMoradia() {}

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

    public BigDecimal getRendaMensal() {
        return rendaMensal;
    }

    public void setRendaMensal(BigDecimal rendaMensal) {
        this.rendaMensal = rendaMensal;
    }

    public BigDecimal getReserva() {
        return reserva;
    }

    public void setReserva(BigDecimal reserva) {
        this.reserva = reserva;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm;
    }
}
