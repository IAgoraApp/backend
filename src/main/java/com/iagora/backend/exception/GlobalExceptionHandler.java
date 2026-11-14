package com.iagora.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<Map<String, String>> tratarRegraNegocio(
        RegraNegocioException erro
    ) {
        Map<String, String> resposta = Map.of(
            "erro", erro.getMessage()
        );

        return ResponseEntity
            .status(erro.getStatus())
            .body(resposta);
    }
}