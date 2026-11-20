package com.iagora.backend.modules.comercial.metodoPagamento;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "MetodoPagamento", description = "Operações de MetodoPagamento.")
public class MetodoPagamentoController {
    private final MetodoPagamentoService service;
    private final boolean pagamentosTeste;

    public MetodoPagamentoController(MetodoPagamentoService service,
            @Value("${iagora.pagamentos.teste:false}") boolean pagamentosTeste) {
        this.service = service;
        this.pagamentosTeste = pagamentosTeste;
    }

    @PostMapping("/metodos-pagamento")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagamentoResponse cadastrar(@RequestBody MetodoPagamentoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @PostMapping("/metodos-pagamento/teste")
    @ResponseStatus(HttpStatus.CREATED)
    public MetodoPagamentoResponse cadastrarTeste(@RequestBody MetodoPagamentoRequest request, Authentication authentication) {
        if (!pagamentosTeste) {
            throw new RegraNegocioException("Pagamentos de teste estão desabilitados", HttpStatus.SERVICE_UNAVAILABLE);
        }
        var dados = new MetodoPagamentoRequest("cartao", request.apelido(), request.bandeira(),
            request.ultimosDigitos(), "teste-" + UUID.randomUUID(), null, null);
        return service.cadastrar(dados, usuario(authentication));
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
