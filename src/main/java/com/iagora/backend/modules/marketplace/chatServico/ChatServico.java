package com.iagora.backend.modules.marketplace.chatServico;

import com.iagora.backend.modules.identidade.perfil.Perfil;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresa;
import com.iagora.backend.modules.marketplace.servicoContratado.ServicoContratado;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "chat_servico")
public class ChatServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "empresa_id", nullable = false)
    private PerfilEmpresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "contratado_id", unique = true)
    private ServicoContratado contratado;

    public ChatServico() {}

    public Integer getId() {
        return id;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public PerfilEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(PerfilEmpresa empresa) {
        this.empresa = empresa;
    }

    public ServicoContratado getContratado() {
        return contratado;
    }

    public void setContratado(ServicoContratado contratado) {
        this.contratado = contratado;
    }

    @Column(name = "oculto_cliente", nullable = false)
    private boolean ocultoCliente;

    @Column(name = "oculto_empresa", nullable = false)
    private boolean ocultoEmpresa;

    public boolean isOcultoCliente() {
        return ocultoCliente;
    }

    public void setOcultoCliente(boolean ocultoCliente) {
        this.ocultoCliente = ocultoCliente;
    }

    public boolean isOcultoEmpresa() {
        return ocultoEmpresa;
    }

    public void setOcultoEmpresa(boolean ocultoEmpresa) {
        this.ocultoEmpresa = ocultoEmpresa;
    }
}
