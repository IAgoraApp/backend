package com.iagora.backend.modules.comercial.assinatura;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Assinatura", description = "Operações de Assinatura.")
public class AssinaturaController {
    private final AssinaturaService service;
    private final AdministracaoService administracao;

    public AssinaturaController(AssinaturaService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/assinaturas")
    @ResponseStatus(HttpStatus.CREATED)
    public AssinaturaResponse cadastrar(@RequestBody AssinaturaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/assinaturas/minha")
    public AssinaturaResponse minha(Authentication authentication) {
        return service.minha(usuario(authentication));
    }

    @GetMapping("/assinaturas/historico")
    public List<AssinaturaResponse> historico(Authentication authentication) {
        return service.historico(usuario(authentication));
    }

    @GetMapping("/assinaturas/{id}")
    public AssinaturaResponse consultar(@PathVariable Integer id, Authentication authentication) {
        return service.consultar(id, usuario(authentication));
    }

    @PatchMapping("/assinaturas/{id}/cancelar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancelar(@PathVariable Integer id, Authentication authentication) {
        service.cancelar(id, usuario(authentication));
    }

    @PatchMapping("/assinaturas/{id}/alterar-plano")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarPlano(@PathVariable Integer id, @RequestBody AssinaturaRequest request, Authentication authentication) {
        service.alterarPlano(id, request, usuario(authentication));
    }

    @GetMapping("/admin/assinaturas")
    public List<AssinaturaResponse> listarTodas(Authentication authentication) {
        administracao.verificar(authentication);
        return service.listarTodas();
    }

    @GetMapping("/admin/assinaturas/{id}")
    public AssinaturaResponse consultarAdmin(@PathVariable Integer id, Authentication authentication) {
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
