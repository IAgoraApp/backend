package com.iagora.backend.modules.casa.tarefa.tarefa;

import com.iagora.backend.modules.casa.tarefa.categoriaTarefa.CategoriaTarefa;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tarefa")
public class Tarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)

    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "categoria_id", nullable = false)
    private CategoriaTarefa categoria;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "prazo")
    private LocalDateTime prazo;

    @Column(name = "lembrete_minutos")
    private Integer lembreteMinutos;

    public Tarefa() {}

    public Integer getLembreteMinutos() {
        return lembreteMinutos;
    }

    public void setLembreteMinutos(Integer lembreteMinutos) {
        this.lembreteMinutos = lembreteMinutos;
    }

    public Integer getId() {
        return id;
    }

    public CategoriaTarefa getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTarefa categoria) {
        this.categoria = categoria;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDateTime prazo) {
        this.prazo = prazo;
    }
}
