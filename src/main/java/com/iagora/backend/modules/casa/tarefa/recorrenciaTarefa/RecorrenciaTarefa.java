package com.iagora.backend.modules.casa.tarefa.recorrenciaTarefa;

import com.iagora.backend.modules.casa.tarefa.tarefa.Tarefa;
import jakarta.persistence.*;
import java.time.LocalDate;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "recorrencia_tarefa")
public class RecorrenciaTarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "tarefa_id", nullable = false, unique = true)
    private Tarefa tarefa;

    @Column(name = "frequencia", nullable = false, length = 30)
    private String frequencia;

    @Column(name = "intervalo", nullable = false)
    private Integer intervalo = 1;

    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "dias_semana")
    private JsonNode diasSemana;

    @JdbcTypeCode(SqlTypes.JSON)

    @Column(name = "dia_mes")
    private JsonNode diaMes;

    @Column(name = "inicio", nullable = false)
    private LocalDate inicio;

    @Column(name = "fim")
    private LocalDate fim;

    public RecorrenciaTarefa() {}

    public Integer getId() {
        return id;
    }

    public Tarefa getTarefa() {
        return tarefa;
    }

    public void setTarefa(Tarefa tarefa) {
        this.tarefa = tarefa;
    }

    public String getFrequencia() {
        return frequencia;
    }

    public void setFrequencia(String frequencia) {
        this.frequencia = frequencia;
    }

    public Integer getIntervalo() {
        return intervalo;
    }

    public void setIntervalo(Integer intervalo) {
        this.intervalo = intervalo;
    }

    public JsonNode getDiasSemana() {
        return diasSemana;
    }

    public void setDiasSemana(JsonNode diasSemana) {
        this.diasSemana = diasSemana;
    }

    public JsonNode getDiaMes() {
        return diaMes;
    }

    public void setDiaMes(JsonNode diaMes) {
        this.diaMes = diaMes;
    }

    public LocalDate getInicio() {
        return inicio;
    }

    public void setInicio(LocalDate inicio) {
        this.inicio = inicio;
    }

    public LocalDate getFim() {
        return fim;
    }

    public void setFim(LocalDate fim) {
        this.fim = fim;
    }

    @Column(name = "processada_ate")
    private LocalDate processadaAte;

    public LocalDate getProcessadaAte() {
        return processadaAte;
    }

    public void setProcessadaAte(LocalDate processadaAte) {
        this.processadaAte = processadaAte;
    }
}
