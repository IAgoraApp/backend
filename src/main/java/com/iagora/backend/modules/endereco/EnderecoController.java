package com.iagora.backend.modules.endereco;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/enderecos")
public class EnderecoController {

  private final EnderecoService enderecoService;

  public EnderecoController(EnderecoService enderecoService) {
    this.enderecoService = enderecoService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public EnderecoResponse cadastrar(@RequestBody EnderecoRequest request) {
    return enderecoService.cadastrar(request);
  }

  @GetMapping("/{id}")
  public EnderecoResponse buscarPorId(@PathVariable Integer id) {
    return enderecoService.buscarPorId(id);
  }

  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(
    @PathVariable Integer id,
    @RequestBody EnderecoRequest request
  ) {
    enderecoService.atualizar(id, request);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagarEndereco(@PathVariable Integer id) {
    enderecoService.apagarEndereco(id);
  }
}
