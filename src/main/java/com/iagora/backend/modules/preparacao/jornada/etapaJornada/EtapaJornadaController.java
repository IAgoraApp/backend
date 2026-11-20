package com.iagora.backend.modules.preparacao.jornada.etapaJornada;

import com.iagora.backend.exception.RegraNegocioException;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Etapa Jornada", description = "Operações de etapa jornada")
public class EtapaJornadaController {

    private final EtapaJornadaService service;

    public EtapaJornadaController(EtapaJornadaService service) {
        this.service = service;
    }

    @PostMapping("/jornadas/{jornadaId}/etapas")
    @ResponseStatus(HttpStatus.CREATED)
    public EtapaJornadaResponse cadastrar(@PathVariable Integer jornadaId, @RequestBody EtapaJornadaRequest request, Authentication authentication) {
        return service.cadastrar(jornadaId, request, usuario(authentication));
    }

    @GetMapping("/jornadas/{jornadaId}/etapas")
    public List<EtapaJornadaResponse> listarPorJornada(@PathVariable Integer jornadaId, Authentication authentication) {
        return service.listarPorJornada(jornadaId, usuario(authentication));
    }

    @GetMapping("/etapas-jornada/{id}")
    public EtapaJornadaResponse buscarPorId(@PathVariable Integer id, Authentication authentication) {
        return service.buscarPorId(id, usuario(authentication));
    }

    @PatchMapping("/etapas-jornada/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizar(@PathVariable Integer id, @RequestBody EtapaJornadaRequest request, Authentication authentication) {
        service.atualizar(id, request, usuario(authentication));
    }

    @PatchMapping("/etapas-jornada/{id}/concluir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void concluir(@PathVariable Integer id, Authentication authentication) {
        service.concluir(id, usuario(authentication));
    }

    @PatchMapping("/etapas-jornada/{id}/reabrir")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reabrir(@PathVariable Integer id, Authentication authentication) {
        service.reabrir(id, usuario(authentication));
    }

    @DeleteMapping("/etapas-jornada/{id}")
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

    @PatchMapping("/jornadas/{jornadaId}/etapas/ordenar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ordenar(@PathVariable Integer jornadaId, @RequestBody OrdenarEtapaJornadaRequest request, Authentication authentication) {
        service.ordenar(jornadaId, request, usuario(authentication));
    }

    @GetMapping("/etapas-jornada/{id}/progresso")
    public EtapaJornadaProgressoResponse progresso(@PathVariable Integer id, Authentication authentication) {
        return service.progresso(id, usuario(authentication));
    }
}
