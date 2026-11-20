package com.iagora.backend.modules.marketplace.perfilEmpresa;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.type.SqlTypes;
import tools.jackson.databind.JsonNode;

@Entity
@Table(name = "perfil_empresa")
public class PerfilEmpresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "endereco_id")
    private Integer enderecoId;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "cnpj", nullable = false, unique = true, length = 14)
    private String cnpj;

    @Column(name = "descricao", columnDefinition = "text")
    private String descricao;

    @Column(name = "categoria", nullable = false, length = 150)
    private String categoria;

    @Column(name = "logo", length = 500)
    private String logo;

    @Column(name = "banner", length = 500)
    private String banner;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "local_atendimento")
    private JsonNode localAtendimento;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "horario_atendimento")
    private JsonNode horarioAtendimento;

    @Column(name = "email_comercial", nullable = false, length = 255)
    private String emailComercial;

    @Column(name = "site", length = 500)
    private String site;

    @Column(name = "data_criacao", nullable = false)
    private LocalDateTime dataCriacao;

    @Column(name = "validado", nullable = false)
    private Boolean validado = false;

    @Column(name = "status_validacao", nullable = false, length = 30)
    private String statusValidacao = "rascunho";

    public PerfilEmpresa() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Integer getEnderecoId() {
        return enderecoId;
    }

    public void setEnderecoId(Integer enderecoId) {
        this.enderecoId = enderecoId;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getBanner() {
        return banner;
    }

    public void setBanner(String banner) {
        this.banner = banner;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public JsonNode getLocalAtendimento() {
        return localAtendimento;
    }

    public void setLocalAtendimento(JsonNode localAtendimento) {
        this.localAtendimento = localAtendimento;
    }

    public JsonNode getHorarioAtendimento() {
        return horarioAtendimento;
    }

    public void setHorarioAtendimento(JsonNode horarioAtendimento) {
        this.horarioAtendimento = horarioAtendimento;
    }

    public String getEmailComercial() {
        return emailComercial;
    }

    public void setEmailComercial(String emailComercial) {
        this.emailComercial = emailComercial;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public LocalDateTime getDataCriacao() {
        return dataCriacao;
    }

    public void setDataCriacao(LocalDateTime dataCriacao) {
        this.dataCriacao = dataCriacao;
    }

    public Boolean getValidado() {
        return validado;
    }

    public void setValidado(Boolean validado) {
        this.validado = validado;
    }

    public String getStatusValidacao() {
        return statusValidacao;
    }

    public void setStatusValidacao(String statusValidacao) {
        this.statusValidacao = statusValidacao;
    }
}
