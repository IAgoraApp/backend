package com.iagora.backend.modules.preparacao.aprendizado.alternativa;

import com.iagora.backend.modules.preparacao.aprendizado.questao.Questao;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "alternativa")
public class Alternativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Questao questao;

    @Column(columnDefinition = "text", nullable = false)
    private String texto;

    @Column(nullable = false)
    private Boolean correta = false;

    public Alternativa() {}

    public Integer getId() {
        return id;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public String getTexto() {
        return texto;
    }

    public void setTexto(String texto) {
        this.texto = texto;
    }

    public Boolean getCorreta() {
        return correta;
    }

    public void setCorreta(Boolean correta) {
        this.correta = correta;
    }
}
