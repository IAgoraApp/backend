package com.iagora.backend.modules.preparacao.moradia.categoriaCustoInicial;

public record CategoriaCustoInicialResponse(Integer id, Integer planoId, Integer casaDesejadaId,
                                            String categoria, String titulo, String foto, boolean oculta) {}
