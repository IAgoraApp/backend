package com.iagora.backend.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
@ConditionalOnProperty(
    name = "security.jwt.enabled",
    havingValue = "true",
    matchIfMissing = true
)
public class MethodSecurityConfig {

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("#p0.toString() == authentication.name")
    public @interface SomenteProprietario {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@perfilService.buscarPorId(#p0).userId().toString() == authentication.name")
    public @interface SomenteProprietarioPerfil {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@casaService.buscarPorId(#p0).userId().toString() == authentication.name")
    public @interface SomenteProprietarioCasa {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("#p0.casaId() == null || @casaService.buscarPorId(#p0.casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioCasaRequest {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@casaService.buscarPorId(@comprasService.buscarPorId(#p0).casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioCompras {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@casaService.buscarPorId(@estoqueItemService.buscarPorId(#p0).casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioEstoque {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("#p0.userId() != null && #p0.userId().toString() == authentication.name")
    public @interface SomenteProprietarioReceitaRequest {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@receitaService.proprietario(#p0, authentication.name)")
    public @interface SomenteProprietarioReceita {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@receitaService.podeLer(#p0, authentication != null ? authentication.name : null)")
    public @interface PodeLerReceita {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@casaService.buscarPorId(@comodoService.buscarPorId(#p0).casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioComodo {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("#p0.comodoId() == null || @casaService.buscarPorId(@comodoService.buscarPorId(#p0.comodoId()).casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioComodoRequest {
    }

    @Target(ElementType.METHOD)
    @Retention(RetentionPolicy.RUNTIME)
    @PreAuthorize("@casaService.buscarPorId(@comodoService.buscarPorId(@limpezaService.buscarPorId(#p0).comodoId()).casaId()).userId().toString() == authentication.name")
    public @interface SomenteProprietarioLimpeza {
    }
}
