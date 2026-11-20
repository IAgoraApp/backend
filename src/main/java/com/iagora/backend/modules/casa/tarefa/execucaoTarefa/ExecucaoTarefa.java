package com.iagora.backend.modules.casa.tarefa.execucaoTarefa;

import com.iagora.backend.modules.casa.tarefa.tarefa.Tarefa;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "execucao_tarefa")
public class ExecucaoTarefa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)

    @JoinColumn(name = "tarefa_id", nullable = false)
    private Tarefa tarefa;

    @Column(name = "data_prevista", nullable = false)
    private LocalDateTime dataPrevista;

    @Column(name = "concluida_em")
    private LocalDateTime concluidaEm;

    @Column(name = "status", nullable = false, length = 30)
    private String status = "pendente";

    public ExecucaoTarefa() {}

    public Integer getId() {
        return id;
    }

    public Tarefa getTarefa() {
        return tarefa;
    }

    public void setTarefa(Tarefa tarefa) {
        this.tarefa = tarefa;
    }

    public LocalDateTime getDataPrevista() {
        return dataPrevista;
    }

    public void setDataPrevista(LocalDateTime dataPrevista) {
        this.dataPrevista = dataPrevista;
    }

    public LocalDateTime getConcluidaEm() {
        return concluidaEm;
    }

    public void setConcluidaEm(LocalDateTime concluidaEm) {
        this.concluidaEm = concluidaEm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Column(nullable = false)
    private boolean gerada;

    public void setGerada(boolean gerada) {
        this.gerada = gerada;
    }
}
