package com.iagora.backend.modules.marketplace.perfilEmpresa;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.endereco.EnderecoRequest;
import com.iagora.backend.modules.endereco.EnderecoResponse;
import com.iagora.backend.security.AdministracaoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "PerfilEmpresa", description = "Operações de PerfilEmpresa.")
public class PerfilEmpresaController {
    private final PerfilEmpresaService service;
    private final AdministracaoService administracao;

    public PerfilEmpresaController(PerfilEmpresaService service, AdministracaoService administracao) {
        this.service = service;
        this.administracao = administracao;
    }

    @PostMapping("/empresas/perfil")
    @ResponseStatus(HttpStatus.CREATED)
    public PerfilEmpresaResponse cadastrar(@RequestBody PerfilEmpresaRequest request, Authentication authentication) {
        return service.cadastrar(request, usuario(authentication));
    }

    @GetMapping("/empresas/meu-perfil")
    public PerfilEmpresaResponse meuPerfil(Authentication authentication) {
        return service.meuPerfil(usuario(authentication));
    }

    @PatchMapping("/empresas/meu-perfil")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@RequestBody PerfilEmpresaRequest request, Authentication authentication) {
        service.atualizar(request, usuario(authentication));
    }

    @GetMapping("/empresas/meu-perfil/endereco")
    public EnderecoResponse consultarEndereco(Authentication authentication) {
        return service.consultarEndereco(usuario(authentication));
    }

    @PatchMapping("/empresas/meu-perfil/endereco")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void editarEndereco(@RequestBody EnderecoRequest endereco, Authentication authentication) {
        service.editarEndereco(usuario(authentication), endereco);
    }

    @PostMapping("/empresas/meu-perfil/enviar-validacao")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enviarValidacao(Authentication authentication) {
        service.enviarValidacao(usuario(authentication));
    }

    @GetMapping("/empresas")
    public List<PerfilEmpresaResponse> listar() {
        return service.listar();
    }

    @GetMapping("/empresas/{id}")
    public PerfilEmpresaResponse detalhar(@PathVariable Integer id) {
        return service.detalhar(id);
    }

    @GetMapping("/admin/empresas/pendentes")
    public List<PerfilEmpresaResponse> pendentes(Authentication authentication) {
        administracao.verificar(authentication);
        return service.pendentes();
    }

    @PatchMapping("/admin/empresas/{id}/aprovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void aprovar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.aprovar(id);
    }

    @PatchMapping("/admin/empresas/{id}/reprovar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reprovar(@PathVariable Integer id, Authentication authentication) {
        administracao.verificar(authentication);
        service.reprovar(id);
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
