package com.iagora.backend.shared;

import com.iagora.backend.exception.RegraNegocioException;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import javax.imageio.ImageIO;
import javax.imageio.stream.MemoryCacheImageInputStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.multipart.MultipartFile;

public final class ImagemUpload {
    private static final int TAMANHO_MAXIMO = 5 * 1024 * 1024;
    private static final int DIMENSAO_MAXIMA = 4096;

    private ImagemUpload() {}

    public static byte[] ler(MultipartFile arquivo) {
        if (arquivo.isEmpty() || arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("Envie uma imagem de até 5 MB", HttpStatus.BAD_REQUEST);
        }
        try {
            return arquivo.getBytes();
        } catch (IOException ex) {
            throw imagemInvalida();
        }
    }

    public static String identificar(byte[] conteudo) {
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

    private static void validarWebp(byte[] conteudo) {
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

    private static int inteiro24(byte[] conteudo, int inicio) {
        return Byte.toUnsignedInt(conteudo[inicio]) | (Byte.toUnsignedInt(conteudo[inicio + 1]) << 8)
            | (Byte.toUnsignedInt(conteudo[inicio + 2]) << 16);
    }

    private static String codigo(byte[] conteudo, int inicio) {
        return new String(conteudo, inicio, 4, StandardCharsets.US_ASCII);
    }

    private static void validarDimensoes(int largura, int altura) {
        if (largura < 1 || altura < 1 || largura > DIMENSAO_MAXIMA || altura > DIMENSAO_MAXIMA) {
            throw new RegraNegocioException("Dimensões da imagem inválidas", HttpStatus.BAD_REQUEST);
        }
    }

    private static RegraNegocioException imagemInvalida() {
        return new RegraNegocioException("Envie uma imagem JPG, PNG ou WebP válida", HttpStatus.BAD_REQUEST);
    }
}
