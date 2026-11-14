package com.iagora.backend.modules.identidade.autenticacao;
import jakarta.persistence.*;

@Entity 
@Table (name= "users")
public class User {
    @Id 
    @GeneratedValue (strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column (nullable = false, unique = true)
    private String email;

    @Column (nullable = false)
    private String senha;

    @Column (nullable = false)
    private Integer tipo;

    public  User(){}

    public User(String email, String senha, Integer tipo){
        this.email = email;
        this.senha = senha;
        this.tipo = tipo;
    }

    public Integer getId(){
        return id;
    }

    public String getEmail(){
        return email;
    }

    public String getSenha(){
        return senha;
    }

    public Integer getTipo(){
        return tipo;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public void setSenha(String senha){
        this.senha = senha;
    }

    public void setTipo(Integer tipo){
        this.tipo = tipo;
    }
}

