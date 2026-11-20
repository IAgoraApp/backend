package com.iagora.backend.modules.casa.cozinha.receita;

import com.iagora.backend.modules.casa.cozinha.receitaItem.ReceitaItemRequest;
import java.util.List;

public record ReceitaRequest(
    Integer userId,
    Boolean publica,
    String nome,
    String descricao,
    String tipo,
    String preparo,
    String imagem,
    String statusPublicacao,
    List<ReceitaItemRequest> ingredientes
) {
    public ReceitaRequest(Integer userId, Boolean publica, String nome, String descricao, String tipo, String preparo) {
        this(userId, publica, nome, descricao, tipo, preparo, null, null, null);
    }
}
