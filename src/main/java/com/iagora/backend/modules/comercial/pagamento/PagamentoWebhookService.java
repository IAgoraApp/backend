package com.iagora.backend.modules.comercial.pagamento;

import com.iagora.backend.exception.RegraNegocioException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.DateTimeException;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Service
public class PagamentoWebhookService {

    private final PagamentoService pagamentoService;
    private final ObjectMapper mapper;
    private final byte[] segredo;

    public PagamentoWebhookService(PagamentoService pagamentoService, ObjectMapper mapper,
                                    @Value("${iagora.pagamentos.webhook.segredo:}") String segredo) {
        this.pagamentoService = pagamentoService;
        this.mapper = mapper;
        this.segredo = Base64.getDecoder().decode(segredo);
        if (this.segredo.length != 0 && this.segredo.length < 32) {
            throw new IllegalArgumentException("Configure um segredo de webhook de pelo menos 32 bytes em Base64");
        }
    }

    public void processar(String timestamp, String assinatura, InputStream entrada) throws IOException {
        if (segredo.length == 0) {
            throw new RegraNegocioException("Webhook de pagamentos desabilitado", HttpStatus.SERVICE_UNAVAILABLE);
        }
        byte[] corpo = entrada.readNBytes(8193);
        if (corpo.length > 8192) {
            throw new RegraNegocioException("Evento de pagamento excede o limite permitido", HttpStatus.PAYLOAD_TOO_LARGE);
        }
        verificar(timestamp, assinatura, corpo);
        try {
            pagamentoService.processarEvento(mapper.readValue(corpo, EventoPagamentoRequest.class));
        } catch (JacksonException ex) {
            throw new RegraNegocioException("Evento de pagamento inválido", HttpStatus.BAD_REQUEST);
        }
    }

    private void verificar(String timestamp, String assinatura, byte[] corpo) {
        if (timestamp == null || assinatura == null) {
            throw semAutorizacao();
        }
        try {
            var momento = Instant.ofEpochSecond(Long.parseLong(timestamp));
            if (Duration.between(momento, Instant.now()).abs().compareTo(Duration.ofMinutes(5)) > 0) {
                throw semAutorizacao();
            }
            var mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(segredo, "HmacSHA256"));
            mac.update((timestamp + ".").getBytes(StandardCharsets.UTF_8));
            if (!MessageDigest.isEqual(mac.doFinal(corpo), Base64.getDecoder().decode(assinatura))) {
                throw semAutorizacao();
            }
        } catch (IllegalArgumentException | DateTimeException ex) {
            throw semAutorizacao();
        } catch (GeneralSecurityException ex) {
            throw new IllegalStateException("Falha ao verificar assinatura do webhook", ex);
        }
    }

    private RegraNegocioException semAutorizacao() {
        return new RegraNegocioException("Assinatura de webhook inválida", HttpStatus.UNAUTHORIZED);
    }
}
