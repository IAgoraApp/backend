package com.iagora.backend.modules.casa.cozinha.receita;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.iagora.backend.security.MethodSecurityConfig.PodeLerReceita;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietario;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioReceita;
import com.iagora.backend.security.MethodSecurityConfig.SomenteProprietarioReceitaRequest;
import java.util.List;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Cozinha - Receitas", description = "Cadastro, consulta e edição de receitas.")
@RestController
@RequestMapping("/receitas")
public class ReceitaController {

  private final ReceitaService receitaService;

  public ReceitaController(ReceitaService receitaService) {
    this.receitaService = receitaService;
  }

  @SomenteProprietarioReceitaRequest
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaResponse cadastrar(@RequestBody ReceitaRequest request) {
    return receitaService.cadastrar(request);
  }

  @SomenteProprietarioReceitaRequest
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ReceitaResponse cadastrarComImagem(@RequestPart("receita") ReceitaRequest request,
      @RequestPart("arquivo") MultipartFile arquivo) {
    return receitaService.cadastrar(request, arquivo);
  }

  @PodeLerReceita
  @GetMapping("/{id}/imagem")
  public ResponseEntity<byte[]> imagem(@PathVariable Integer id) {
    var imagem = receitaService.buscarImagem(id);
    return ResponseEntity.ok()
      .cacheControl(CacheControl.noStore())
      .header("X-Content-Type-Options", "nosniff")
      .contentType(MediaType.parseMediaType(imagem.getTipo()))
      .body(imagem.getConteudo());
  }

  @GetMapping
  public List<ReceitaResponse> listar() {
    return receitaService.listar();
  }

  @PodeLerReceita
  @GetMapping("/{id}")
  public ReceitaResponse buscarPorId(@PathVariable Integer id) {
    return receitaService.buscarPorId(id);
  }

  @SomenteProprietario
  @GetMapping("/user/{userId}")
  public List<ReceitaResponse> buscarPorUserId(@PathVariable Integer userId) {
    return receitaService.buscarPorUserId(userId);
  }

  @SomenteProprietarioReceita
  @PatchMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizar(@PathVariable Integer id, @RequestBody ReceitaRequest request) {
    receitaService.atualizar(id, request);
  }

  @SomenteProprietarioReceita
  @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void atualizarComImagem(@PathVariable Integer id, @RequestPart("receita") ReceitaRequest request,
      @RequestPart("arquivo") MultipartFile arquivo) {
    receitaService.atualizar(id, request, arquivo);
  }

  @SomenteProprietarioReceita
  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void apagar(@PathVariable Integer id) {
    receitaService.apagar(id);
  }
}
