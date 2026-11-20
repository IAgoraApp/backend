package com.iagora.backend.modules.marketplace.servicoFavorito;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.marketplace.servico.ServicoResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "ServicoFavorito", description = "Operações de ServicoFavorito.")
public class ServicoFavoritoController {
    private final ServicoFavoritoService service;

    public ServicoFavoritoController(ServicoFavoritoService service) {
        this.service = service;
    }

    @GetMapping("/servicos-favoritos")
    public List<ServicoResponse> listar(Authentication authentication) {
        return service.listar(usuario(authentication));
    }

    @PutMapping("/servicos/{id}/favorito")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void adicionar(@PathVariable Integer id, Authentication authentication) {
        service.adicionar(id, usuario(authentication));
    }

    @DeleteMapping("/servicos/{id}/favorito")
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
