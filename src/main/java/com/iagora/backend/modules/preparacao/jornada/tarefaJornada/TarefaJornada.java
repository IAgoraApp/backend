package com.iagora.backend.modules.preparacao.jornada.tarefaJornada;

import com.iagora.backend.modules.preparacao.jornada.etapaJornada.EtapaJornada;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "tarefa_jornada")
public class TarefaJornada {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "etapa_id", nullable = false)
    private EtapaJornada etapa;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "concluida", nullable = false)
    private Boolean concluida = false;

    @Column(name = "ordem", nullable = false)
    private Integer ordem;

    public TarefaJornada() {}

    public Integer getId() {
        return id;
    }

    public EtapaJornada getEtapa() {
        return etapa;
    }

    public void setEtapa(EtapaJornada etapa) {
        this.etapa = etapa;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getConcluida() {
        return concluida;
    }

    public void setConcluida(Boolean concluida) {
        this.concluida = concluida;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }
}
