package com.iagora.backend.modules.financas.contaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.financas.transacaoFinanceira.TransacaoFinanceiraResponse;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Conta Finanças", description = "Operações de conta finanças")
public class ContaFinanceiraController {

    private final ContaFinanceiraService service;

    public ContaFinanceiraController(ContaFinanceiraService service) {
        this.service = service;
    }

    @PostMapping("/contas-financeiras")
    @ResponseStatus(HttpStatus.CREATED)
    public ContaFinanceiraResponse cadastrar(@RequestBody ContaFinanceiraRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/contas-financeiras/{id}")
    public ContaFinanceiraResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/contas-financeiras/usuario/{userId}")
    public List<ContaFinanceiraResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/contas-financeiras/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody ContaFinanceiraRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @DeleteMapping("/contas-financeiras/{id}")
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

    @GetMapping("/contas-financeiras/{id}/extrato")
    public List<TransacaoFinanceiraResponse> extrato(@PathVariable Integer id, Authentication authentication) {
        return service.extrato(id, usuario(authentication));
    }
}
