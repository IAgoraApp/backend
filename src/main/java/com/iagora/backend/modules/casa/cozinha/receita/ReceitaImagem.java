package com.iagora.backend.modules.casa.cozinha.receita;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "receita_imagem")
public class ReceitaImagem {
    @Id
    private Integer id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "receita_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Receita receita;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Lob
    @Column(nullable = false)
    private byte[] conteudo;

    protected ReceitaImagem() {}

    ReceitaImagem(Receita receita) {
        this.receita = receita;
    }

    void substituir(String tipo, byte[] conteudo) {
        this.tipo = tipo;
        this.conteudo = conteudo;
    }

    String getTipo() {
        return tipo;
    }

    byte[] getConteudo() {
        return conteudo;
    }
}
