package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import com.iagora.backend.modules.preparacao.moradia.custoMoradia.CustoMoradiaRequest;
import java.util.List;

public record CustosMensaisRequest(Integer planoId, Integer casaDesejadaId, List<CustoMoradiaRequest> custos) {}
