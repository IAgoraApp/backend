package com.iagora.backend.modules.preparacao.moradia.custoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Custo Moradia", description = "Operações de custo moradia")
public class CustoMoradiaController {

    private final CustoMoradiaService service;

    public CustoMoradiaController(CustoMoradiaService service) {
        this.service = service;
    }

    @PostMapping("/planos-moradia/{planoId}/custos")
    @ResponseStatus(HttpStatus.CREATED)
    public CustoMoradiaResponse cadastrar(@PathVariable Integer planoId, @RequestBody CustoMoradiaRequest request, Authentication authentication) {
        return service.cadastrar(planoId, request, usuario(authentication));
    }

    @GetMapping("/planos-moradia/{planoId}/custos")
    public List<CustoMoradiaResponse> listarPorPlano(@PathVariable Integer planoId, Authentication authentication) {
        return service.listarPorPlano(planoId, usuario(authentication));
    }

    @GetMapping("/custos-moradia/{id}")
    public CustoMoradiaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/custos-moradia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody CustoMoradiaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/custos-moradia/{id}")
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
