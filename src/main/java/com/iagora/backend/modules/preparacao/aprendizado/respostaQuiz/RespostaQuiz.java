package com.iagora.backend.modules.preparacao.aprendizado.respostaQuiz;

import com.iagora.backend.modules.preparacao.aprendizado.alternativa.Alternativa;
import com.iagora.backend.modules.preparacao.aprendizado.questao.Questao;
import com.iagora.backend.modules.preparacao.aprendizado.tentativaQuiz.TentativaQuiz;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "resposta_quiz", uniqueConstraints = @UniqueConstraint(columnNames = {"tentativa_id", "questao_id"}))
public class RespostaQuiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tentativa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TentativaQuiz tentativa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "questao_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Questao questao;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "alternativa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Alternativa alternativa;

    public RespostaQuiz() {}

    public Integer getId() {
        return id;
    }

    public TentativaQuiz getTentativa() {
        return tentativa;
    }

    public void setTentativa(TentativaQuiz tentativa) {
        this.tentativa = tentativa;
    }

    public Questao getQuestao() {
        return questao;
    }

    public void setQuestao(Questao questao) {
        this.questao = questao;
    }

    public Alternativa getAlternativa() {
        return alternativa;
    }

    public void setAlternativa(Alternativa alternativa) {
        this.alternativa = alternativa;
    }
}
