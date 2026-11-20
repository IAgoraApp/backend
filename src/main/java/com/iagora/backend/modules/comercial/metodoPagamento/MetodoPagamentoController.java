package com.iagora.backend.modules.comercial.metodoPagamento;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "MetodoPagamento", description = "Operações de MetodoPagamento.")
public class MetodoPagamentoController {
    private final MetodoPagamentoService service;

    public MetodoPagamentoController(MetodoPagamentoService service) {
        this.service = service;
    }

    @PostMapping("/metodos-pagamento")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagamentoResponse cadastrar(@RequestBody MetodoPagamentoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/metodos-pagamento")
    public List<MetodoPagamentoResponse> listar(Authentication authentication) {
        return service.listar(usuario(authentication));
    }

    @GetMapping("/metodos-pagamento/{id}")
    public MetodoPagamentoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @PatchMapping("/metodos-pagamento/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody ApelidoMetodoPagamentoRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/metodos-pagamento/{id}/principal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void principal(@PathVariable Integer id, Authentication authentication) {
        service.principal(id, usuario(authentication));
    }

    @PatchMapping("/metodos-pagamento/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id, Authentication authentication) {
        service.desativar(id, usuario(authentication));
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
