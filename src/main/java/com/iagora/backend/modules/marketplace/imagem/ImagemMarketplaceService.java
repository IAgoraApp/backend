package com.iagora.backend.modules.marketplace.imagem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresa;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaService;
import com.iagora.backend.modules.marketplace.servico.Servico;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import com.iagora.backend.shared.ImagemUpload;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagemMarketplaceService {

    private static final String CAMINHO = "/imagens-marketplace/";

    private final ImagemMarketplaceRepository repository;
    private final PerfilEmpresaService empresaService;
    private final ServicoService servicoService;
    private final EntityManager entityManager;

    public ImagemMarketplaceService(ImagemMarketplaceRepository repository, PerfilEmpresaService empresaService,
                                     ServicoService servicoService, EntityManager entityManager) {
        this.repository = repository;
        this.empresaService = empresaService;
        this.servicoService = servicoService;
        this.entityManager = entityManager;
    }

    @Transactional
    public ImagemResponse salvarEmpresa(MultipartFile arquivo, Integer userId) {
        var empresa = empresaService.buscarDoUsuario(userId);
        entityManager.refresh(empresa, LockModeType.PESSIMISTIC_WRITE);
        return salvar(empresa, null, arquivo);
    }

    @Transactional
    public ImagemResponse salvarServico(Integer id, MultipartFile arquivo, Integer userId) {
        var servico = servicoService.buscarDoUsuario(id, userId);
        entityManager.refresh(servico, LockModeType.PESSIMISTIC_WRITE);
        if (!List.of("rascunho", "reprovado").contains(servico.getStatusPublicacao())) {
            throw new RegraNegocioException("Envie imagens enquanto o serviço está em edição", HttpStatus.CONFLICT);
        }
        return salvar(servico.getEmpresa(), servico, arquivo);
    }

    @Transactional
    public ImagemMarketplace consultar(UUID id, Integer userId) {
        var imagem = repository.findById(id).orElseThrow(this::naoEncontrada);
        var empresa = imagem.getEmpresa();
        if (empresa.getUser().getId().equals(userId)) {
            return imagem;
        }
        var servico = imagem.getServico();
        String url = CAMINHO + id;
        boolean visivel = servico == null ? empresa.getValidado() && vinculada(empresa, null, url)
            : servicoService.visivel(servico, LocalDateTime.now()) && vinculada(empresa, servico, url);
        if (!visivel) {
            throw naoEncontrada();
        }
        return imagem;
    }

    private ImagemResponse salvar(PerfilEmpresa empresa, Servico servico, MultipartFile arquivo) {
        byte[] conteudo = ImagemUpload.ler(arquivo);
        String tipo = ImagemUpload.identificar(conteudo);
        var referencias = servico == null
            ? repository.findByEmpresa_IdAndServicoIsNull(empresa.getId()) : repository.findByServico_Id(servico.getId());
        LocalDateTime expirar = LocalDateTime.now().minusHours(24);
        List<UUID> expiradas = new ArrayList<>();
        int pendentes = 0;
        for (var referencia : referencias) {
            if (!vinculada(empresa, servico, CAMINHO + referencia.getId())) {
                if (referencia.getCriadaEm().isBefore(expirar)) {
                    expiradas.add(referencia.getId());
                } else {
                    pendentes++;
                }
            }
        }
        if (pendentes >= (servico == null ? 2 : 8)) {
            throw new RegraNegocioException("Limite de imagens pendentes para este cadastro", HttpStatus.CONFLICT);
        }
        if (!expiradas.isEmpty()) {
            repository.deleteByIdIn(expiradas);
        }
        var imagem = repository.save(new ImagemMarketplace(empresa, servico, tipo, conteudo));
        return new ImagemResponse(CAMINHO + imagem.getId());
    }

    private boolean vinculada(PerfilEmpresa empresa, Servico servico, String url) {
        if (servico == null) {
            return url.equals(empresa.getLogo()) || url.equals(empresa.getBanner());
        }
        if (servico.getFotos() != null) {
            for (var foto : servico.getFotos()) {
                if (url.equals(foto.asText())) {
                    return true;
                }
            }
        }
        return false;
    }

    private RegraNegocioException naoEncontrada() {
        return new RegraNegocioException("Imagem não encontrada", HttpStatus.NOT_FOUND);
    }
}
