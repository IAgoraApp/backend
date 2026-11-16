package com.iagora.backend.modules.casa.cozinha.receitaUsuario;

import com.iagora.backend.modules.casa.cozinha.receita.Receita;
import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;

@Entity
@Table(name = "receita_usuario", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "receita_id" }))
public class ReceitaUsuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    @Column(nullable = false)
    private String status = "fazer";

    public ReceitaUsuario() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Receita getReceita() {
        return receita;
    }

    public void setReceita(Receita receita) {
        this.receita = receita;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
