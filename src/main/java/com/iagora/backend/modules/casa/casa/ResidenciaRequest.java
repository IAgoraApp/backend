package com.iagora.backend.modules.casa.casa;

import com.iagora.backend.modules.endereco.EnderecoRequest;

public record ResidenciaRequest(String nome, String tipo, EnderecoRequest endereco) {}
