package com.iagora.backend.modules.endereco;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.exception.RegraNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Endereços", description = "Endereços utilizados pelos módulos da plataforma.")
@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

  private final EnderecoService enderecoService;

  public EnderecoController(EnderecoService enderecoService) {
    this.enderecoService = enderecoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EnderecoResponse cadastrar(@RequestBody EnderecoRequest request, Authentication authentication) {
    return enderecoService.cadastrar(request, usuario(authentication));
  }

  @GetMapping("/{id}")
  public EnderecoResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
    enderecoService.verificarProprietario(id, usuario(authentication));
    return enderecoService.buscarPorId(id);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(
    @PathVariable Integer id,
    @RequestBody EnderecoRequest request,
    Authentication authentication
  ) {
    enderecoService.verificarProprietario(id, usuario(authentication));
    enderecoService.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagarEndereco(@PathVariable Integer id, Authentication authentication) {
    enderecoService.verificarProprietario(id, usuario(authentication));
    enderecoService.apagarEndereco(id);
  }
  private Integer usuario(Authentication authentication) {
    if (authentication == null) {
      throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
    }
    return Integer.valueOf(authentication.getName());
  }
}
