package com.iagora.backend.modules.comercial.pagamento;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "Webhook de pagamentos", description = "Recebimento de eventos assinados para cobranças de teste.")
public class PagamentoWebhookController {

    private final PagamentoWebhookService service;

    public PagamentoWebhookController(PagamentoWebhookService service) {
        this.service = service;
    }

    @PostMapping(value = "/pagamentos/webhook", consumes = "application/json")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void receber(@RequestHeader(value = "X-IAgora-Timestamp", required = false) String timestamp,
                        @RequestHeader(value = "X-IAgora-Assinatura", required = false) String assinatura,
                        InputStream corpo) throws IOException {
        service.processar(timestamp, assinatura, corpo);
    }
}
