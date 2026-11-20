package com.iagora.backend.modules.marketplace.imagem;

import com.iagora.backend.exception.RegraNegocioException;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresa;
import com.iagora.backend.modules.marketplace.perfilEmpresa.PerfilEmpresaService;
import com.iagora.backend.modules.marketplace.servico.Servico;
import com.iagora.backend.modules.marketplace.servico.ServicoService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.transaction.Transactional;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagemMarketplaceService {

    private static final int TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final int DIMENSAO_MAXIMA = 4096;
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
        byte[] conteudo = ler(arquivo);
        String tipo = identificar(conteudo);
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

    private byte[] ler(MultipartFile arquivo) {
        if (arquivo.isEmpty() || arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("Envie uma imagem de até 5 MB", HttpStatus.BAD_REQUEST);
        }
        try {
            return arquivo.getBytes();
        } catch (IOException ex) {
            throw imagemInvalida();
        }
    }

    private String identificar(byte[] conteudo) {
        if (conteudo.length >= 20 && "RIFF".equals(codigo(conteudo, 0)) && "WEBP".equals(codigo(conteudo, 8))) {
            validarWebp(conteudo);
            return "image/webp";
        }
        try (var entrada = new MemoryCacheImageInputStream(new ByteArrayInputStream(conteudo))) {
            var leitores = ImageIO.getImageReaders(entrada);
            if (leitores.hasNext()) {
                var leitor = leitores.next();
                try {
                    String tipo = switch (leitor.getFormatName().toLowerCase(java.util.Locale.ROOT)) {
                        case "jpeg" -> "image/jpeg";
                        case "png" -> "image/png";
                        default -> throw imagemInvalida();
                    };
                    leitor.setInput(entrada, true, true);
                    validarDimensoes(leitor.getWidth(0), leitor.getHeight(0));
                    BufferedImage imagem = leitor.read(0);
                    imagem.flush();
                    return tipo;
                } finally {
                    leitor.dispose();
                }
            }
        } catch (IOException | IllegalArgumentException ex) {
            throw imagemInvalida();
        }
        throw imagemInvalida();
    }

    private void validarWebp(byte[] conteudo) {
        var buffer = ByteBuffer.wrap(conteudo).order(ByteOrder.LITTLE_ENDIAN);
        if (buffer.getInt(4) != conteudo.length - 8) {
            throw imagemInvalida();
        }
        boolean dadosImagem = false;
        for (int pos = 12; pos < conteudo.length; ) {
            if (conteudo.length - pos < 8) {
                throw imagemInvalida();
            }
            String formato = codigo(conteudo, pos);
            int tamanho = buffer.getInt(pos + 4);
            int dados = pos + 8;
            if (tamanho < 0 || (long) dados + tamanho + (tamanho & 1) > conteudo.length) {
                throw imagemInvalida();
            }
            switch (formato) {
                case "VP8X" -> {
                    if (tamanho != 10 || (conteudo[dados] & 2) != 0) {
                        throw imagemInvalida();
                    }
                    validarDimensoes(1 + inteiro24(conteudo, dados + 4), 1 + inteiro24(conteudo, dados + 7));
                }
                case "VP8 " -> {
                    if (tamanho < 10 || (conteudo[dados] & 1) != 0 || inteiro24(conteudo, dados + 3) != 0x2a019d) {
                        throw imagemInvalida();
                    }
                    validarDimensoes(buffer.getShort(dados + 6) & 0x3fff, buffer.getShort(dados + 8) & 0x3fff);
                    dadosImagem = true;
                }
                case "VP8L" -> {
                    if (tamanho < 5 || Byte.toUnsignedInt(conteudo[dados]) != 0x2f) {
                        throw imagemInvalida();
                    }
                    int dimensoes = buffer.getInt(dados + 1);
                    validarDimensoes((dimensoes & 0x3fff) + 1, ((dimensoes >>> 14) & 0x3fff) + 1);
                    dadosImagem = true;
                }
                default -> { }
            }
            pos = dados + tamanho + (tamanho & 1);
        }
        if (!dadosImagem) {
            throw imagemInvalida();
        }
    }

    private int inteiro24(byte[] conteudo, int inicio) {
        return Byte.toUnsignedInt(conteudo[inicio]) | (Byte.toUnsignedInt(conteudo[inicio + 1]) << 8)
            | (Byte.toUnsignedInt(conteudo[inicio + 2]) << 16);
    }

    private String codigo(byte[] conteudo, int inicio) {
        return new String(conteudo, inicio, 4, StandardCharsets.US_ASCII);
    }

    private void validarDimensoes(int largura, int altura) {
        if (largura < 1 || altura < 1 || largura > DIMENSAO_MAXIMA || altura > DIMENSAO_MAXIMA) {
            throw new RegraNegocioException("Dimensões da imagem inválidas", HttpStatus.BAD_REQUEST);
        }
    }

    private RegraNegocioException imagemInvalida() {
        return new RegraNegocioException("Envie uma imagem JPG, PNG ou WebP válida", HttpStatus.BAD_REQUEST);
    }

    private RegraNegocioException naoEncontrada() {
        return new RegraNegocioException("Imagem não encontrada", HttpStatus.NOT_FOUND);
    }
}
