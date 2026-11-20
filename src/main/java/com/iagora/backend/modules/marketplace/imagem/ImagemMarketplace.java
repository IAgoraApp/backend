package com.iagora.backend.modules.marketplace.imagem;

import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresa;
import com.iagora.backend.modules.marketplace.servico.Servico;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "imagem_marketplace")
public class ImagemMarketplace {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PerfilEmpresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servico_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Servico servico;

    @Column(nullable = false, length = 20)
    private String tipo;

    @Column(name = "criada_em", nullable = false)
    private LocalDateTime criadaEm;

    @Lob
    @Column(nullable = false)
    private byte[] conteudo;

    public ImagemMarketplace() {}

    public ImagemMarketplace(PerfilEmpresa empresa, Servico servico, String tipo, byte[] conteudo) {
        this.id = UUID.randomUUID();
        this.empresa = empresa;
        this.servico = servico;
        this.tipo = tipo;
        this.conteudo = conteudo;
        this.criadaEm = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public PerfilEmpresa getEmpresa() {
        return empresa;
    }

    public Servico getServico() {
        return servico;
    }

    public String getTipo() {
        return tipo;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public byte[] getConteudo() {
        return conteudo;
    }
}
