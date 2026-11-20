package com.iagora.backend.modules.casa.cozinha.compras;

import jakarta.persistence.*;
import java.time.LocalDate;

import com.iagora.backend.modules.casa.casa.Casa;

@Entity
@Table(name = "compras")
public class Compras {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "casa_id", nullable = false)
    private Casa casa;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false)
    private LocalDate criacao = LocalDate.now();

    public Compras() {}

    public Integer getId() {
        return id;
    }

    public Casa getCasa() {
        return casa;
    }

    public void setCasa(Casa casa) {
        this.casa = casa;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public LocalDate getCriacao() {
        return criacao;
    }

    public void setCriacao(LocalDate criacao) {
        this.criacao = criacao;
    }
}
