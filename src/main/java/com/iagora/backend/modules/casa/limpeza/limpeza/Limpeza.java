package com.iagora.backend.modules.casa.limpeza.limpeza;

import com.iagora.backend.modules.casa.limpeza.comodo.Comodo;
import java.time.LocalDate;
import jakarta.persistence.*;

@Entity
@Table(name = "limpezas")
public class Limpeza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comodo_id", nullable = false)
    private Comodo comodo;

    @Column(nullable = false)
    private String nome;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "ultima_vez")
    private LocalDate ultimaVez;

    @Column(name = "proxima_vez")
    private LocalDate proximaVez;

    public Limpeza() {}

    public Integer getId() {
        return id;
    }

    public Comodo getComodo() {
        return comodo;
    }

    public void setComodo(Comodo comodo) {
        this.comodo = comodo;
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

    public LocalDate getUltimaVez() {
        return ultimaVez;
    }

    public void setUltimaVez(LocalDate ultimaVez) {
        this.ultimaVez = ultimaVez;
    }

    public LocalDate getProximaVez() {
        return proximaVez;
    }

    public void setProximaVez(LocalDate proximaVez) {
        this.proximaVez = proximaVez;
    }
}
