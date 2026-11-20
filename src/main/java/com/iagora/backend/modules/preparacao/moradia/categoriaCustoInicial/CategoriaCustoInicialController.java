package com.iagora.backend.modules.preparacao.moradia.categoriaCustoInicial;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categorias-custo-inicial")
@Tag(name = "Categoria de custo inicial", description = "Categorias e preferências de custos iniciais por imóvel ou plano.")
public class CategoriaCustoInicialController {

    private final CategoriaCustoInicialService service;

    public CategoriaCustoInicialController(CategoriaCustoInicialService service) {
        this.service = service;
    }

    @GetMapping
    public List<CategoriaCustoInicialResponse> listar(@RequestParam(required = false) Integer planoId,
                                                      @RequestParam(required = false) Integer casaDesejadaId,
                                                      Authentication authentication) {
        return service.listar(usuario(authentication), planoId, casaDesejadaId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaCustoInicialResponse cadastrar(@RequestBody CategoriaCustoInicialRequest request,
                                                    Authentication authentication) {
        return service.cadastrar(usuario(authentication), request);
    }

    @DeleteMapping("/{categoria}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void apagar(@PathVariable String categoria, @RequestParam(required = false) Integer planoId,
                       @RequestParam(required = false) Integer casaDesejadaId, Authentication authentication) {
        service.apagar(usuario(authentication), planoId, casaDesejadaId, categoria);
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
