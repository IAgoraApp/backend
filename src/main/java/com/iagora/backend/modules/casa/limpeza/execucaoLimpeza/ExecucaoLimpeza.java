package com.iagora.backend.modules.casa.limpeza.execucaoLimpeza;

import com.iagora.backend.modules.casa.limpeza.limpeza.Limpeza;
import com.iagora.backend.modules.identidade.perfil.Perfil;
import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "execucao_limpeza")
public class ExecucaoLimpeza {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "limpeza_id", nullable = false)
    private Limpeza limpeza;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @Column(name = "realizada_em", nullable = false)
    private LocalDateTime realizadaEm;

    public ExecucaoLimpeza() {}

    public Integer getId() {
        return id;
    }

    public Limpeza getLimpeza() {
        return limpeza;
    }

    public void setLimpeza(Limpeza limpeza) {
        this.limpeza = limpeza;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public LocalDateTime getRealizadaEm() {
        return realizadaEm;
    }

    public void setRealizadaEm(LocalDateTime realizadaEm) {
        this.realizadaEm = realizadaEm;
    }
}
