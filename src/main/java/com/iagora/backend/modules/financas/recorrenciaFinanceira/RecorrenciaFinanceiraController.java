package com.iagora.backend.modules.financas.recorrenciaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Recorrencia Finanças", description = "Operações de recorrencia finanças")
public class RecorrenciaFinanceiraController {

    private final RecorrenciaFinanceiraService service;

    public RecorrenciaFinanceiraController(RecorrenciaFinanceiraService service) {
        this.service = service;
    }

    @PostMapping("/recorrencias-financeiras")
    @ResponseStatus(HttpStatus.CREATED)
    public RecorrenciaFinanceiraResponse cadastrar(@RequestBody RecorrenciaFinanceiraRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/recorrencias-financeiras/{id}")
    public RecorrenciaFinanceiraResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/recorrencias-financeiras/usuario/{userId}")
    public List<RecorrenciaFinanceiraResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/recorrencias-financeiras/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody RecorrenciaFinanceiraRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/recorrencias-financeiras/{id}/ativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ativar(@PathVariable Integer id, Authentication authentication) {
        service.ativar(id, usuario(authentication));
    }

    @PatchMapping("/recorrencias-financeiras/{id}/desativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void desativar(@PathVariable Integer id, Authentication authentication) {
        service.desativar(id, usuario(authentication));
    }

    @DeleteMapping("/recorrencias-financeiras/{id}")
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
