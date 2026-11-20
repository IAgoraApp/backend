package com.iagora.backend.modules.casa.cozinha.receitaFavorita;

import jakarta.persistence.*;

import com.iagora.backend.modules.casa.cozinha.receita.Receita;
import com.iagora.backend.modules.identidade.autenticacao.User;

@Entity
@Table(name = "receita_favorita", uniqueConstraints = @UniqueConstraint(columnNames = { "user_id", "receita_id" }))
public class ReceitaFavorita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receita_id", nullable = false)
    private Receita receita;

    public ReceitaFavorita() {}

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
}
