package com.iagora.backend.modules.marketplace.mensagem;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.marketplace.chatServico.ChatServico;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "mensagem")
public class Mensagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "chat_id", nullable = false)
    private ChatServico chat;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "mensagem", nullable = false, columnDefinition = "text")
    private String mensagem;

    @Column(name = "data", nullable = false)
    private LocalDateTime data;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    public Mensagem() {}

    public Integer getId() {
        return id;
    }

    public ChatServico getChat() {
        return chat;
    }

    public void setChat(ChatServico chat) {
        this.chat = chat;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public LocalDateTime getData() {
        return data;
    }

    public void setData(LocalDateTime data) {
        this.data = data;
    }

    public LocalDateTime getLidaEm() {
        return lidaEm;
    }

    public void setLidaEm(LocalDateTime lidaEm) {
        this.lidaEm = lidaEm;
    }
}
