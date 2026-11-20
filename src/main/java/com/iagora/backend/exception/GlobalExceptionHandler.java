package com.iagora.backend.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> tratarIntegridade(DataIntegrityViolationException erro) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(Map.of("erro", "Registro duplicado ou vinculado a outros dados"));
    }
}