package com.iagora.backend.modules.preparacao.moradia.planoMoradia;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Plano Moradia", description = "Operações de plano moradia")
public class PlanoMoradiaController {

    private final PlanoMoradiaService service;

    public PlanoMoradiaController(PlanoMoradiaService service) {
        this.service = service;
    }

    @PostMapping("/planos-moradia")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanoMoradiaResponse cadastrar(@RequestBody PlanoMoradiaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @PutMapping("/planos-moradia/custos-mensais")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void salvarCustosMensais(@RequestBody CustosMensaisRequest request, Authentication authentication) {
        service.salvarCustosMensais(request, usuario(authentication));
    }

    @GetMapping("/planos-moradia/{id}")
    public PlanoMoradiaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @SomenteProprietario
    @GetMapping("/planos-moradia/user/{userId}")
    public List<PlanoMoradiaResponse> listarPorUsuario(@PathVariable Integer userId, Authentication authentication) {
        return service.listarPorUsuario(usuario(authentication));
    }

    @PatchMapping("/planos-moradia/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody PlanoMoradiaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/planos-moradia/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/planos-moradia/{id}/arquivar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void arquivar(@PathVariable Integer id, Authentication authentication) {
        service.arquivar(id, usuario(authentication));
    }

    @PatchMapping("/planos-moradia/{id}/reativar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reativar(@PathVariable Integer id, Authentication authentication) {
        service.reativar(id, usuario(authentication));
    }

    @DeleteMapping("/planos-moradia/{id}")
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

    @GetMapping("/planos-moradia/{id}/resumo")
    public ResumoPlanoMoradiaResponse resumo(@PathVariable Integer id, Authentication authentication) {
        return service.resumo(id, usuario(authentication));
    }
}
