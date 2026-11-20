package com.iagora.backend.modules.comercial.assinatura;

import com.iagora.backend.modules.comercial.planoAssinatura.PlanoAssinatura;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "assinatura")
public class Assinatura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "plano_id", nullable = false)
    private PlanoAssinatura plano;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "pendente";

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "proxima_cobranca")
    private LocalDateTime proximaCobranca;

    @Column(name = "cancelada_em")
    private LocalDateTime canceladaEm;

    public Assinatura() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlanoAssinatura getPlano() {
        return plano;
    }

    public void setPlano(PlanoAssinatura plano) {
        this.plano = plano;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getInicio() {
        return inicio;
    }

    public void setInicio(LocalDateTime inicio) {
        this.inicio = inicio;
    }

    public LocalDateTime getProximaCobranca() {
        return proximaCobranca;
    }

    public void setProximaCobranca(LocalDateTime proximaCobranca) {
        this.proximaCobranca = proximaCobranca;
    }

    public LocalDateTime getCanceladaEm() {
        return canceladaEm;
    }

    public void setCanceladaEm(LocalDateTime canceladaEm) {
        this.canceladaEm = canceladaEm;
    }
}
