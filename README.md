<div align="center">

# IAGORA | Backend

### Sua independência começa aqui.

API REST do **IAGORA**, uma plataforma desenvolvida para facilitar a jornada de quem deseja morar sozinho ou já conquistou sua independência.

O backend é responsável pelo **gerenciamento dos dados, processamento das regras de negócio e integração com o banco de dados**, oferecendo a estrutura necessária para os módulos e funcionalidades da plataforma.

### Tecnologias

![Java](https://img.shields.io/badge/Java-20232A?style=for-the-badge&logo=openjdk&logoColor=ED8B00)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-20232A?style=for-the-badge&logo=springboot&logoColor=6DB33F)
![MySQL](https://img.shields.io/badge/MySQL-20232A?style=for-the-badge&logo=mysql&logoColor=4479A1)
![Maven](https://img.shields.io/badge/Maven-20232A?style=for-the-badge&logo=apachemaven&logoColor=C71A36)

---

<sub>Desenvolvido pela equipe IAGORA.</sub>

</div>

## Módulos da referência do frontend

Execute `./mvnw test` com Java 25. Os testes de integração usam H2 em modo MySQL e exercitam a API com JWT habilitado.

| Variável de ambiente | Finalidade |
| --- | --- |
| `GEMINI_API_KEY` | Chave do Google AI Studio, configurada apenas no ambiente do backend. |
| `GEMINI_MODEL` | Modelo do assistente. O padrão é `gemini-3.1-flash-lite`. |
| `SECURITY_JWT_ENABLED=true` | Habilita a autenticação Bearer. É o padrão da aplicação. |
| `IAGORA_CORS_ORIGENS` | Origens CORS separadas por vírgula. |
| `IAGORA_FRONTEND_URL` | Origem do frontend usada nos links de recuperação de senha. |
| `SPRING_PROFILES_ACTIVE=demo` e `IAGORA_DEMO_ENABLED=true` | Habilitam a carga explícita do catálogo no banco de demonstração. |
| `IAGORA_ADMINISTRACAO_USUARIOS` | IDs de contas administrativas existentes, separados por vírgula. Sem valor, as operações administrativas ficam restritas. |
| `IAGORA_PAGAMENTOS_TESTE=true` | Habilita a criação e a confirmação de cobranças simuladas. O padrão é desabilitado. |
| `IAGORA_PAGAMENTOS_WEBHOOK_SEGREDO` | Chave de pelo menos 32 bytes codificada em Base64, usada pelo webhook de teste. |

Para ativar o chat em `/ia` e `/ia-b`, configure a chave no ambiente do backend e reinicie o processo. Use um projeto da camada gratuita no [Google AI Studio](https://aistudio.google.com/apikey). A disponibilidade e a cota dependem do projeto e do modelo. Consulte os [preços do Gemini](https://ai.google.dev/gemini-api/docs/pricing).

A rota `POST /assistente/conversas` usa o JWT para identificar a conta. O Gemini recebe as mensagens recentes e uma seleção de registros da própria conta consultados automaticamente. A camada gratuita pode usar o conteúdo enviado para melhorar produtos do Google. As propostas de receitas, tarefas e lançamentos financeiros só são salvas quando confirmadas no chat.

O histórico compartilhado por `/ia` e `/ia-b` fica no banco da conta. A API autenticada `/assistente/historico` lista, salva e exclui conversas do titular.

Os uploads de empresa e serviço recebem um campo multipart `arquivo`, com até 5 MB em JPG, PNG ou WebP. A resposta fornece `url`, que pode ser salva em `logo`, `banner` ou `fotos`. Para consultar a imagem, use o endereço retornado com o JWT do usuário.

O webhook de teste recebe um JSON com `transacaoExternaId`, `status` (`pago` ou `cancelado`) e `valor`. Envie a data Unix em segundos no cabeçalho `X-IAgora-Timestamp`. O cabeçalho `X-IAgora-Assinatura` recebe o HMAC-SHA256 de `timestamp.corpo` em Base64. A assinatura usa os bytes exatos do corpo e é válida por cinco minutos.

A rota de recuperação de senha é `POST /user/forgot-password`.

A configuração local, o inventário Swagger e o provisionamento de contas demo
estão no [relatório de integração](../INTEGRACAO_FULLSTACK.md).
