package com.iagora.backend.modules.marketplace.imagem;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Tag(name = "Imagens do marketplace", description = "Envio e consulta de imagens de empresas e serviços.")
public class ImagemMarketplaceController {

    private final ImagemMarketplaceService service;

    public ImagemMarketplaceController(ImagemMarketplaceService service) {
        this.service = service;
    }

    @PostMapping(value = "/empresas/meu-perfil/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemResponse salvarEmpresa(@RequestParam MultipartFile arquivo, Authentication authentication) {
        return service.salvarEmpresa(arquivo, usuario(authentication));
    }

    @PostMapping(value = "/servicos/{id}/imagens", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ImagemResponse salvarServico(@PathVariable Integer id, @RequestParam MultipartFile arquivo,
                                         Authentication authentication) {
        return service.salvarServico(id, arquivo, usuario(authentication));
    }

    @GetMapping("/imagens-marketplace/{id}")
    public ResponseEntity<byte[]> consultar(@PathVariable UUID id, Authentication authentication) {
        var imagem = service.consultar(id, usuario(authentication));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
            .header("X-Content-Type-Options", "nosniff")
            .contentType(MediaType.parseMediaType(imagem.getTipo()))
            .body(imagem.getConteudo());
    }

    private Integer usuario(Authentication authentication) {
        if (authentication == null) {
            throw new RegraNegocioException("Faça login para continuar", HttpStatus.UNAUTHORIZED);
        }
        return Integer.valueOf(authentication.getName());
    }
}
