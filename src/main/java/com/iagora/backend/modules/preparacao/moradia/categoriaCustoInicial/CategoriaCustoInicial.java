package com.iagora.backend.modules.preparacao.moradia.categoriaCustoInicial;

import com.iagora.backend.modules.identidade.autenticacao.User;
import com.iagora.backend.modules.preparacao.moradia.casaDesejada.CasaDesejada;
import com.iagora.backend.modules.preparacao.moradia.planoMoradia.PlanoMoradia;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "categoria_custo_inicial", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "plano_id", "categoria"}),
    @UniqueConstraint(columnNames = {"user_id", "casa_desejada_id", "categoria"})
})
public class CategoriaCustoInicial {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "plano_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PlanoMoradia plano;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "casa_desejada_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private CasaDesejada casaDesejada;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(length = 60)
    private String titulo;

    @Column(length = 150)
    private String foto;

    @Column(nullable = false)
    private Boolean oculta = false;

    public CategoriaCustoInicial() {}

    public Integer getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public PlanoMoradia getPlano() {
        return plano;
    }

    public void setPlano(PlanoMoradia plano) {
        this.plano = plano;
    }

    public CasaDesejada getCasaDesejada() {
        return casaDesejada;
    }

    public void setCasaDesejada(CasaDesejada casaDesejada) {
        this.casaDesejada = casaDesejada;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Boolean getOculta() {
        return oculta;
    }

    public void setOculta(Boolean oculta) {
        this.oculta = oculta;
    }
}
