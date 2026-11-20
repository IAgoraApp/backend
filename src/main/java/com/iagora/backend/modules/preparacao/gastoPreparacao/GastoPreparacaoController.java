package com.iagora.backend.modules.preparacao.gastoPreparacao;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Gasto Atual da Preparação", description = "Operações de gasto atual da preparação")
public class GastoPreparacaoController {

    private final GastoPreparacaoService service;

    public GastoPreparacaoController(GastoPreparacaoService service) {
        this.service = service;
    }

    @PostMapping("/meu-dinheiro/gastos")
    @ResponseStatus(HttpStatus.CREATED)
    public GastoPreparacaoResponse cadastrar(@RequestBody GastoPreparacaoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/meu-dinheiro/gastos")
    public List<GastoPreparacaoResponse> listar(Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/meu-dinheiro/gastos/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody GastoPreparacaoRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/meu-dinheiro/gastos/{id}")
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
