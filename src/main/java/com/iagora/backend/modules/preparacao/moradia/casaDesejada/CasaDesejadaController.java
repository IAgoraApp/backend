package com.iagora.backend.modules.preparacao.moradia.casaDesejada;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Casa Desejada", description = "Operações de casa desejada")
public class CasaDesejadaController {

    private final CasaDesejadaService service;

    public CasaDesejadaController(CasaDesejadaService service) {
        this.service = service;
    }

    @PostMapping("/casas-desejadas")
    @ResponseStatus(HttpStatus.CREATED)
    public CasaDesejadaResponse cadastrar(@RequestBody CasaDesejadaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/casas-desejadas")
    public List<CasaDesejadaResponse> listar(Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @GetMapping("/casas-desejadas/{id}")
    public CasaDesejadaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/casas-desejadas/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody CasaDesejadaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/casas-desejadas/{id}")
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
