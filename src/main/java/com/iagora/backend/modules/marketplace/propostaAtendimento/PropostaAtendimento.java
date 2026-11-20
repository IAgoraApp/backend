package com.iagora.backend.modules.marketplace.propostaAtendimento;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.servicoContratado.ServicoContratado;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "proposta_atendimento")
public class PropostaAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "contratado_id", nullable = false)
    private ServicoContratado contratado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "autor_user_id", nullable = false)
    private User autorUser;

    @Column(name = "data_agendamento", nullable = false)
    private LocalDateTime dataAgendamento;

    @Column(name = "valor", nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "pendente";

    public PropostaAtendimento() {}

    public Integer getId() {
        return id;
    }

    public ServicoContratado getContratado() {
        return contratado;
    }

    public void setContratado(ServicoContratado contratado) {
        this.contratado = contratado;
    }

    public User getAutorUser() {
        return autorUser;
    }

    public void setAutorUser(User autorUser) {
        this.autorUser = autorUser;
    }

    public LocalDateTime getDataAgendamento() {
        return dataAgendamento;
    }

    public void setDataAgendamento(LocalDateTime dataAgendamento) {
        this.dataAgendamento = dataAgendamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
