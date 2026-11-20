package com.iagora.backend.modules.marketplace.perfilEmpresa;

import tools.jackson.databind.JsonNode;

public record PerfilEmpresaRequest(
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
    String site
) {}
