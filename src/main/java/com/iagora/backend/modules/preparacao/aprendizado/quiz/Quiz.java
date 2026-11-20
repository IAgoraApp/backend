package com.iagora.backend.modules.preparacao.aprendizado.quiz;

import com.iagora.backend.modules.preparacao.aprendizado.aula.Aula;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "aula_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Aula aula;

    @Column(length = 150, nullable = false)
    private String titulo;

    public Quiz() {}

    public Integer getId() {
        return id;
    }

    public Aula getAula() {
        return aula;
    }

    public void setAula(Aula aula) {
        this.aula = aula;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }
}
