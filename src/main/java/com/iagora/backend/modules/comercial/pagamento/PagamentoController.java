package com.iagora.backend.modules.comercial.pagamento;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Pagamento", description = "Operações de Pagamento.")
public class PagamentoController {
    private final PagamentoService service;
    private final AdministracaoService administracao;

    public PagamentoController(PagamentoService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/pagamentos")
    @ResponseStatus(HttpStatus.CREATED)
    public PagamentoResponse cadastrar(@RequestBody PagamentoRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/pagamentos")
    public List<PagamentoResponse> listar(Authentication authentication) {
        return service.listar(usuario(authentication));
    }

    @GetMapping("/pagamentos/{id}")
    public PagamentoResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @PostMapping("/pagamentos/{id}/confirmar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void confirmar(@PathVariable Integer id, Authentication authentication) {
        service.confirmar(id, usuario(authentication));
    }

    @PostMapping("/pagamentos/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Integer id, Authentication authentication) {
        service.cancelar(id, usuario(authentication));
    }

    @GetMapping("/admin/pagamentos")
    public List<PagamentoResponse> listarTodos(Authentication authentication) {
        administracao.verificar(authentication);
        return service.listarTodos();
    }

    @GetMapping("/admin/pagamentos/{id}")
    public PagamentoResponse consultarAdmin(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        return service.consultarAdmin(id);
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
