package com.iagora.backend.modules.marketplace.servico;

import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresa;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "servico")
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "empresa_id", nullable = false)
    private PerfilEmpresa empresa;

    @Column(name = "titulo", nullable = false, length = 150)
    private String titulo;

    @Column(name = "descricao", nullable = false, columnDefinition = "text")
    private String descricao;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "fotos")
    private JsonNode fotos;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "preco", precision = 10, scale = 2)
    private BigDecimal preco;

    @Column(name = "tipo_preco", nullable = false, length = 30)
    private String tipoPreco;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "disponibilidade")
    private JsonNode disponibilidade;

    @Column(name = "validado", nullable = false)
    private Boolean validado = false;

    @Column(name = "nota", precision = 3, scale = 2)
    private BigDecimal nota;

    @Column(name = "status_publicacao", nullable = false, length = 30)
    private String statusPublicacao = "rascunho";

    @Column(name = "publicado_em")
    private LocalDateTime publicadoEm;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    public Servico() {}

    public Integer getId() {
        return id;
    }

    public PerfilEmpresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(PerfilEmpresa empresa) {
        this.empresa = empresa;
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

    public JsonNode getFotos() {
        return fotos;
    }

    public void setFotos(JsonNode fotos) {
        this.fotos = fotos;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }

    public String getTipoPreco() {
        return tipoPreco;
    }

    public void setTipoPreco(String tipoPreco) {
        this.tipoPreco = tipoPreco;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public JsonNode getDisponibilidade() {
        return disponibilidade;
    }

    public void setDisponibilidade(JsonNode disponibilidade) {
        this.disponibilidade = disponibilidade;
    }

    public Boolean getValidado() {
        return validado;
    }

    public void setValidado(Boolean validado) {
        this.validado = validado;
    }

    public BigDecimal getNota() {
        return nota;
    }

    public void setNota(BigDecimal nota) {
        this.nota = nota;
    }

    public String getStatusPublicacao() {
        return statusPublicacao;
    }

    public void setStatusPublicacao(String statusPublicacao) {
        this.statusPublicacao = statusPublicacao;
    }

    public LocalDateTime getPublicadoEm() {
        return publicadoEm;
    }

    public void setPublicadoEm(LocalDateTime publicadoEm) {
        this.publicadoEm = publicadoEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }
}
