package com.iagora.backend.modules.casa.cozinha.compras;

import com.iagora.backend.modules.casa.cozinha.compraItem.CompraItemRequest;
import java.util.List;

public record ComprasRequest(Integer casaId, String nome, List<CompraItemRequest> itens) {
    public ComprasRequest(Integer casaId, String nome) {
        this(casaId, nome, null);
    }
}
