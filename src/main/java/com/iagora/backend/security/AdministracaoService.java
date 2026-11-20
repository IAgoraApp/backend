package com.iagora.backend.security;

import com.iagora.backend.exception.RegraNegocioException;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AdministracaoService {

    private final Set<Integer> administradores;

    public AdministracaoService(@Value("${iagora.administracao.usuarios:}") String usuarios) {
        administradores = Arrays.stream(usuarios.split(","))
            .map(String::strip)
            .filter(valor -> !valor.isEmpty())
            .map(Integer::valueOf)
            .collect(Collectors.toUnmodifiableSet());
    }

    public void verificar(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        if (!administradores.contains(Integer.valueOf(authentication.getName()))) {
            throw new RegraNegocioException("Acesso administrativo restrito", HttpStatus.FORBIDDEN);
        }
    }
}
