package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import com.iagora.backend.modules.endereco.EnderecoResponse;
import java.math.BigDecimal;

public record CasaDesejadaResponse(
    Integer id,
    Integer userId,
    Integer enderecoId,
    String titulo,
    String descricao,
    String tipo,
    BigDecimal valor,
    String valorTipo,
    String mobilia,
    EnderecoResponse endereco
) {}
