package com.iagora.backend.modules.preparacao.aprendizado.progressoAula;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.aprendizado.aula.Aula;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "progresso_aula", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "aula_id"}))
public class ProgressoAula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Aula aula;

    @Column(nullable = false)
    private Boolean concluida = false;

    public ProgressoAula() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public Boolean getConcluida() {
        return concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida;
    }
}
