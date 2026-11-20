package com.iagora.backend.modules.marketplace.servicoContratado;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "ServicoContratado", description = "Operações de ServicoContratado.")
public class ServicoContratadoController {
    private final ServicoContratadoService service;

    public ServicoContratadoController(ServicoContratadoService service) {
        this.service = service;
    }

    @PostMapping("/servicos/{servicoId}/contratar")
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoContratadoResponse contratar(@PathVariable Integer servicoId, @RequestBody ServicoContratadoRequest request, Authentication authentication) {
        return service.contratar(servicoId, request, usuario(authentication));
    }

    @GetMapping("/contratacoes")
    public List<ServicoContratadoResponse> minhas(Authentication authentication) {
        return service.minhas(usuario(authentication));
    }

    @GetMapping("/contratacoes/{id}")
    public ServicoContratadoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody ServicoContratadoRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @GetMapping("/empresas/contratacoes")
    public List<ServicoContratadoResponse> recebidas(Authentication authentication) {
        return service.recebidas(usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Integer id, Authentication authentication) {
        service.cancelar(id, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/aceitar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aceitar(@PathVariable Integer id, Authentication authentication) {
        service.aceitar(id, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/recusar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recusar(@PathVariable Integer id, Authentication authentication) {
        service.recusar(id, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/iniciar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void iniciar(@PathVariable Integer id, Authentication authentication) {
        service.iniciar(id, usuario(authentication));
    }

    @PatchMapping("/contratacoes/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
