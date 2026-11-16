
package com.iagora.backend.modules.identidade.perfil;

import com.iagora.backend.modules.identidade.autenticacao.User;
import jakarta.persistence.*;

@Entity
@Table(name = "perfil")
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "endereco_id")
    private Integer enderecoId;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 500)
    private String foto;

    @Column(nullable = false)
    private Integer tipo;

    public Perfil() {}

    public Perfil(User user, String nome, Integer tipo) {
        this.user = user;
        this.nome = nome;
        this.tipo = tipo;
    }

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

    public String getFoto() {
        return foto;
    }

    public void setFoto(String foto) {
        this.foto = foto;
    }

    public Integer getTipo() {
        return tipo;
    }

    public void setTipo(Integer tipo) {
        this.tipo = tipo;
    }
}
