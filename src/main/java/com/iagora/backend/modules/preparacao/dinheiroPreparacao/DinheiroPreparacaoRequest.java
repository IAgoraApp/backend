package com.iagora.backend.modules.preparacao.dinheiroPreparacao;

import tools.jackson.databind.JsonNode;

public record DinheiroPreparacaoRequest(JsonNode renda, JsonNode reserva, JsonNode mudancaPrevista) {}
