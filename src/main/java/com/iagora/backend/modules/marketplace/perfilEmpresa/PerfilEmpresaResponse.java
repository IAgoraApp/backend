package com.iagora.backend.modules.marketplace.perfilEmpresa;

import java.time.LocalDateTime;
import tools.jackson.databind.JsonNode;

public record PerfilEmpresaResponse(
    Integer id,
    Integer userId,
    Integer enderecoId,
    String nome,
    String cnpj,
    String descricao,
    String categoria,
    String logo,
    String banner,
    String telefone,
    JsonNode localAtendimento,
    JsonNode horarioAtendimento,
    String emailComercial,
    String site,
    LocalDateTime dataCriacao,
    Boolean validado,
    String statusValidacao
) {}
