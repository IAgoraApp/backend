package com.iagora.backend.modules.financas.metaFinanceira;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Meta Finanças", description = "Operações de meta finanças")
public class MetaFinanceiraController {

    private final MetaFinanceiraService service;

    public MetaFinanceiraController(MetaFinanceiraService service) {
        this.service = service;
    }

    @PostMapping("/metas-financeiras")
    @ResponseStatus(HttpStatus.CREATED)
    public MetaFinanceiraResponse cadastrar(@RequestBody MetaFinanceiraRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/metas-financeiras/{id}")
    public MetaFinanceiraResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/metas-financeiras/usuario/{userId}")
    public List<MetaFinanceiraResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/metas-financeiras/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody MetaFinanceiraRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/metas-financeiras/{id}/valor")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void valor(@PathVariable Integer id, @RequestBody AporteMetaFinanceiraRequest data, Authentication authentication) {
        service.valor(id, usuario(authentication), data);
    }

    @PatchMapping("/metas-financeiras/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/metas-financeiras/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Integer id, Authentication authentication) {
        service.cancelar(id, usuario(authentication));
    }

    @DeleteMapping("/metas-financeiras/{id}")
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
