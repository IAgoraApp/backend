package com.iagora.backend.modules.catalogo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "catalogo_importado")
public class CatalogoImportado {
    @Id
    @Column(length = 50)
    private String versao;

    protected CatalogoImportado() {}

    CatalogoImportado(String versao) {
        this.versao = versao;
    }
}
