package com.iagora.backend.modules.preparacao.metaMudanca;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Meta da Mudança", description = "Operações de meta da mudança")
public class MetaMudancaController {

    private final MetaMudancaService service;

    public MetaMudancaController(MetaMudancaService service) {
        this.service = service;
    }

    @PostMapping("/metas-mudanca")
    @ResponseStatus(HttpStatus.CREATED)
    public MetaMudancaResponse cadastrar(@RequestBody MetaMudancaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/metas-mudanca")
    public List<MetaMudancaResponse> listar(Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @GetMapping("/metas-mudanca/{id}")
    public MetaMudancaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/metas-mudanca/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody MetaMudancaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/metas-mudanca/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable Integer id, Authentication authentication) {
        service.apagar(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
