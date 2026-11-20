package com.iagora.backend.modules.preparacao.dinheiroPreparacao;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "dinheiro_preparacao")
public class DinheiroPreparacao {

    @Id
    private Integer userId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Column(precision = 12, scale = 2)
    private BigDecimal renda;

    @Column(precision = 12, scale = 2)
    private BigDecimal reserva;

    @Column(name = "mudanca_prevista")
    private LocalDate mudancaPrevista;

    public DinheiroPreparacao() {}

    public DinheiroPreparacao(User user) {
        this.user = user;
    }

    public BigDecimal getRenda() {
        return renda;
    }

    public void setRenda(BigDecimal renda) {
        this.renda = renda;
    }

    public BigDecimal getReserva() {
        return reserva;
    }

    public void setReserva(BigDecimal reserva) {
        this.reserva = reserva;
    }

    public LocalDate getMudancaPrevista() {
        return mudancaPrevista;
    }

    public void setMudancaPrevista(LocalDate mudancaPrevista) {
        this.mudancaPrevista = mudancaPrevista;
    }
}
