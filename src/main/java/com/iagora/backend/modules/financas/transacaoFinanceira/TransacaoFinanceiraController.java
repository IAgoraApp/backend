package com.iagora.backend.modules.financas.transacaoFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Transação Finanças", description = "Operações de transação finanças")
public class TransacaoFinanceiraController {

    private final TransacaoFinanceiraService service;

    public TransacaoFinanceiraController(TransacaoFinanceiraService service) {
        this.service = service;
    }

    @PostMapping("/transacoes-financeiras")
    @ResponseStatus(HttpStatus.CREATED)
    public TransacaoFinanceiraResponse cadastrar(@RequestBody TransacaoFinanceiraRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/transacoes-financeiras/{id}")
    public TransacaoFinanceiraResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/transacoes-financeiras/usuario/{userId}")
    public List<TransacaoFinanceiraResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/transacoes-financeiras/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody TransacaoFinanceiraRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/transacoes-financeiras/{id}/pagar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pagar(@PathVariable Integer id, @RequestBody(required = false) PagarTransacaoRequest data, Authentication authentication) {
        service.pagar(id, usuario(authentication), data);
    }

    @PatchMapping("/transacoes-financeiras/{id}/pendente")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pendente(@PathVariable Integer id, Authentication authentication) {
        service.pendente(id, usuario(authentication));
    }

    @DeleteMapping("/transacoes-financeiras/{id}")
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

    @SomenteProprietario
    @GetMapping("/transacoes-financeiras/usuario/{userId}/resumo")
    public ResumoFinanceiroResponse resumo(@PathVariable Integer userId, Authentication authentication) {
        return service.resumo(usuario(authentication));
    }
}
