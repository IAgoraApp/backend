package com.iagora.backend.modules.marketplace.servicoFavorito;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.servico.Servico;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "servico_favorito")
@IdClass(ServicoFavoritoId.class)
public class ServicoFavorito {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "servico_id", nullable = false)
    private Servico servico;

    public ServicoFavorito() {}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Servico getServico() {
        return servico;
    }

    public void setServico(Servico servico) {
        this.servico = servico;
    }
}
