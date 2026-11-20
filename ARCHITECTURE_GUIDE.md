# Guia de arquitetura do backend IAgora

Este guia orienta a implementação de novas funcionalidades no backend Spring Boot. Use os módulos citados como referência, preserve os contratos existentes e mantenha cada alteração no escopo da funcionalidade.

## 1. Stack e dependências oficiais

O [pom.xml](pom.xml) declara Java **25**, Spring Boot **4.1.2** e Maven. Execute os testes com `./mvnw test` usando o JDK do projeto. As dependências sem versão explícita seguem o gerenciamento do parent do Spring Boot.

| Uso | Dependência declarada |
| --- | --- |
| API REST e JSON | `spring-boot-starter-webmvc` |
| Persistência JPA com Hibernate | `spring-boot-starter-data-jpa` |
| Banco MySQL em execução | `com.mysql:mysql-connector-j` |
| Segurança HTTP e por método | `spring-boot-starter-security` |
| JWT com Spring Security OAuth2 Resource Server e Nimbus JOSE | `spring-boot-starter-oauth2-resource-server` |
| BCrypt | `org.springframework.security:spring-security-crypto` |
| Jakarta Bean Validation | `spring-boot-starter-validation` |
| OpenAPI e Swagger UI | `org.springdoc:springdoc-openapi-starter-webmvc-ui` **3.0.2** |
| Envio de e-mail | `spring-boot-starter-mail` |
| Processamento de anotações | `org.projectlombok:lombok`, opcional e configurado no compilador para código principal e testes |
| Ferramentas de desenvolvimento | `spring-boot-devtools`, opcional em runtime |
| Testes | `spring-boot-starter-data-jpa-test`, `spring-boot-starter-validation-test`, `spring-boot-starter-webmvc-test` e `com.h2database:h2` |

**A adição de dependências ou a alteração de versões exige alinhamento prévio com a equipe.**

O [application.yaml](src/main/resources/application.yaml) configura MySQL por `DB_URL`, `DB_USERNAME` e `DB_PASSWORD`, com `ddl-auto: update`, `show-sql: false` e `open-in-view: false`. O SMTP usa Gmail na porta 587 com STARTTLS e credenciais de `MAIL_USERNAME` e `MAIL_PASSWORD`. O servidor usa `PORT` com padrão 8080 e `forward-headers-strategy: framework`. O Swagger UI usa a raiz `/`. O JWT local usa `JWT_SECRET` e duração de 3.600.000 ms. O login Google lê `GOOGLE_CLIENT_ID` diretamente no serviço.

## 2. Estrutura de pastas e pacotes

```text
src/main/
├── java/com/iagora/backend/
│   ├── BackendApplication.java
│   ├── config/
│   ├── exception/
│   ├── health/
│   ├── modules/
│   │   ├── casa/
│   │   │   ├── casa/
│   │   │   ├── cozinha/
│   │   │   │   ├── compraItem/
│   │   │   │   ├── compras/
│   │   │   │   ├── estoqueItem/
│   │   │   │   ├── produto/
│   │   │   │   ├── receita/
│   │   │   │   ├── receitaFavorita/
│   │   │   │   ├── receitaItem/
│   │   │   │   └── receitaUsuario/
│   │   │   └── limpeza/
│   │   │       ├── comodo/
│   │   │       ├── execucaoLimpeza/
│   │   │       └── limpeza/
│   │   ├── endereco/
│   │   └── identidade/
│   │       ├── autenticacao/
│   │       ├── perfil/
│   │       └── resetSenha/
│   └── security/
└── resources/
    ├── application.yaml
    └── static/
        └── images/
            └── perfil-padrao.svg
```

Os nomes de pacotes abaixo são relativos a `com.iagora.backend`.

| Pacote | Responsabilidade |
| --- | --- |
| Raiz | `BackendApplication`, ponto de entrada do Spring Boot |
| `config` | Configuração transversal de CORS e OpenAPI |
| `exception` | Exceção de negócio e tratamento HTTP global |
| `health` | `HealthController` e verificação da conexão com o banco via `JdbcTemplate` |
| `security` | Cadeia HTTP, configuração de segurança por método e emissão e validação de JWT |
| `modules.casa.casa` | Cadastro interno, consulta, atualização e remoção da casa do usuário |
| `modules.casa.cozinha` | Agrupa os pacotes de lista de compras e itens, estoque, catálogo de produtos, receitas, ingredientes, favoritos e acompanhamento de receitas por usuário |
| `modules.casa.limpeza` | Agrupa os pacotes de cômodos, tarefas de limpeza e registros de execução |
| `modules.endereco` | Cadastro, consulta, atualização e remoção de endereços |
| `modules.identidade.autenticacao` | Conta `User`, persistência de usuários, cadastro, login local e Google, alteração de e-mail e pontos de entrada da recuperação de senha |
| `modules.identidade.perfil` | Criação, consulta, atualização e remoção do perfil pessoal |
| `modules.identidade.resetSenha` | Persistência e ciclo de vida do token de recuperação e envio de e-mail |

Mantenha entidade, repository, service, controller e DTOs no mesmo pacote da funcionalidade, conforme as classes que ela precisar. Coloque validação e mapeamento locais em métodos privados do service. A estrutura atual tem organização por funcionalidade e não contém pacotes globais de controllers, services, repositories, DTOs ou utilitários.

## 3. Padrão do Spring Security (crítico)

### Fluxo de autenticação

- `AuthController` delega cadastro e login a `AuthService`. O service consulta `UserRepository` e usa `BCryptPasswordEncoder` para gravar e conferir a senha. `AuthService.iniciarSessao(User)` usa `JwtService.gerarToken(Integer)` e monta `LoginResponse`.
- `LoginGoogleController` recebe o ID token em `CredencialGoogle`. `LoginGoogleService` valida a credencial com seu `NimbusJwtDecoder` e as chaves públicas do Google. A primeira vinculação exige conta local existente, e-mail Google verificado e confirmação de senha. Depois de identificar a conta, reutiliza `AuthService.iniciarSessao` e entrega o mesmo JWT local.
- `JwtService` usa Nimbus, HS256 e uma chave obtida decodificando `jwt.secret` de Base64. O JWT local contém `sub` com o ID inteiro de `User` serializado como texto, além de `iat` e `exp`. O bean `JwtDecoder` valida esse JWT.
- `SecurityConfig` define a `SecurityFilterChain`. Com JWT habilitado, `oauth2ResourceServer(...jwt(...))` instala o filtro de Bearer Token do Spring Security e usa o decoder local. A cadeia é stateless, usa o bean de `CorsConfig` e tem CSRF, HTTP Basic e form login desabilitados.
- `MethodSecurityConfig` habilita `@EnableMethodSecurity` e concentra as meta-anotações com `@PreAuthorize`. `User` é uma entidade JPA. O repositório não tem filtro JWT próprio, implementação de `UserDetails` ou `UserDetailsService`, conversor de roles ou enum de permissões. `Perfil` representa dados pessoais e `User.tipo` é um atributo de negócio. O JWT local não inclui claims de roles.

Fontes: [SecurityConfig](src/main/java/com/iagora/backend/security/SecurityConfig.java), [JwtService](src/main/java/com/iagora/backend/security/JwtService.java), [MethodSecurityConfig](src/main/java/com/iagora/backend/security/MethodSecurityConfig.java), [autenticacao](src/main/java/com/iagora/backend/modules/identidade/autenticacao).

### Rotas e propriedade

Com `security.jwt.enabled=true`, novas rotas já exigem JWT por `.anyRequest().authenticated()`. Para uma rota explicitamente pública, declare um matcher específico antes dessa regra e limite ao método HTTP quando a exceção se aplicar a um único método. A lista atual libera login, cadastro, recuperação de senha, documentação, saúde e a imagem padrão de perfil. Libera também **somente GET** para `/receitas`, `/receitas/{id}`, `/receitas/{receitaId}/itens` e `/receitas/{receitaId}/itens/{id}`. A listagem pública usa `findByPublicaTrue()` e a leitura individual e dos itens continua sujeita a `@PodeLerReceita`.

Para recursos de um proprietário, aplique no endpoint a meta-anotação que corresponda ao recurso e ao primeiro parâmetro do método (`#p0`). A autenticação HTTP, por si só, não comprova propriedade. Se uma funcionalidade exigir RBAC, alinhe o novo modelo de autorização antes de implementar roles ou tratar `User.tipo` como autoridade.

| Anotação de `MethodSecurityConfig` | Primeiro argumento e critério |
| --- | --- |
| `@SomenteProprietario` | ID de usuário comparado com `authentication.name` |
| `@SomenteProprietarioPerfil` | ID de perfil, cujo `userId()` vem de `PerfilService.buscarPorId` |
| `@SomenteProprietarioCasa` | ID de casa, cujo `userId()` vem de `CasaService.buscarPorId` |
| `@SomenteProprietarioCasaRequest` | Record com `casaId()`. Se preenchido, confere o dono da casa. Se nulo, deixa o service validar o campo |
| `@SomenteProprietarioCompras` | ID de compra. Resolve compra → casa → dono |
| `@SomenteProprietarioEstoque` | ID de item de estoque. Resolve item → casa → dono |
| `@SomenteProprietarioReceitaRequest` | Record com `userId()` preenchido e igual ao ID autenticado |
| `@SomenteProprietarioReceita` | ID de receita. Delega a `ReceitaService.proprietario` |
| `@PodeLerReceita` | ID de receita. Delega a `ReceitaService.podeLer`, que aceita receita pública ou acesso pelo proprietário |
| `@SomenteProprietarioComodo` | ID de cômodo. Resolve cômodo → casa → dono |
| `@SomenteProprietarioComodoRequest` | Record com `comodoId()`. Se preenchido, confere o dono via cômodo → casa. Se nulo, deixa o service validar o campo |
| `@SomenteProprietarioLimpeza` | ID de tarefa de limpeza. Resolve tarefa → cômodo → casa → dono |

### Identidade autenticada e pontos fixos

A forma existente de obter a identidade é `authentication.name` nas expressões de segurança. Os controllers atuais recebem IDs pela rota ou pelo DTO e passam os argumentos já verificados ao service. Para isso, espelhe `AuthController.buscarPorId` ou `ComodoController.buscarPorCasaId`. Caso um endpoint novo precise inferir a identidade sem receber um ID do cliente, receba `Authentication` no controller, converta `getName()` em `Integer` em uma rota com JWT obrigatório e passe o ID ao service. Essa adaptação usa o mesmo `sub` e os controllers atuais ainda não a utilizam. Preserve a posição de `#p0` quando houver uma meta-anotação.

O YAML versionado define **`security.jwt.enabled: false`**. Nesse modo a cadeia permite todas as requisições, o Resource Server fica desativado e `MethodSecurityConfig` não é ativada. Nos testes de autorização, configure `security.jwt.enabled=true`, como em `CozinhaIntegrationTest` e `LimpezaIntegrationTest`. Confirme o valor efetivo no ambiente em que as restrições precisam funcionar. Se a propriedade estiver ausente, as duas configurações Java assumem JWT habilitado.

Preserve a cadeia, a ordem dos matchers, o contrato do JWT, a configuração stateless, o CORS existente, os handlers 401/403 e as meta-anotações atuais. Mude apenas o matcher ou a regra de propriedade estritamente necessária para o novo endpoint, mantendo os contratos das rotas existentes. Preserve a distinção entre o JWT local e a credencial do Google.

## 4. Padrão de implementação por camada

### Entity (JPA)

Use classe concreta com `jakarta.persistence.Entity`, `@Table(name = "...")`, `Integer id`, `@Id`, `@GeneratedValue(strategy = GenerationType.IDENTITY)` e construtor sem argumentos. Escreva getters, setters e construtores explicitamente. Lombok está disponível no build, mas entidades e serviços atuais não adotam `@Data`, `@Getter`, `@Builder` ou `@RequiredArgsConstructor`.

Use nomes de tabela em minúsculas e snake_case quando compostos, como `comodo`, `execucao_limpeza` e `receita_item`. Respeite os nomes existentes, inclusive `users`, `receitas`, `limpezas` e `compras`. Declare `@Column` para restrições e nomes explícitos quando necessário. Use `@JoinColumn` com nomes como `casa_id`, `user_id` e `comodo_id`. O padrão de associação é `@ManyToOne(fetch = FetchType.LAZY)`, com `optional = false` e `nullable = false` para vínculos obrigatórios. `Perfil.user` usa `@OneToOne`. Campos como `Casa.enderecoId` e `TokenReset.userId` são IDs escalares no modelo atual. Para unicidade composta, espelhe `EstoqueItem`, `ReceitaFavorita` ou `ReceitaUsuario`.

### Repository

Declare `interface NomeRepository extends JpaRepository<Nome, Integer>` no pacote da funcionalidade. Use os métodos de CRUD herdados e queries derivadas com os nomes das propriedades Java. Exemplos reais incluem `findByCasa_Id`, `findByPublicaTrue`, `findByEmailIgnoreCase` e `deleteByLimpeza_Comodo_Id`. O sublinhado delimita a navegação na propriedade associada. Use `Optional` para busca unitária derivada e `List` para coleções, conforme a cardinalidade. Os repositories atuais usam métodos herdados e derivados, sem `@Query` customizada.

### DTOs

Use `record` no mesmo pacote, com sufixos `Request` e `Response`. Preserve nomes orientados à operação onde já existem, como `CadastroRequest`, `CompradoRequest` e `FavoritoStatusResponse`. Receba JSON com `@RequestBody` e leia os componentes com acessores como `request.casaId()`. A prática atual valida campos e invariantes no service e lança `RegraNegocioException`. O starter de validação está presente, mas os DTOs e controllers atuais não adotam `@NotNull`, `@NotBlank` ou `@Valid`. Preserve os campos opcionais do PATCH, como em `ComodoService.aplicar`.

Mapeie manualmente no service. Resolva associações pelo repository, aplique campos com setters e monte o response com o construtor do record. Use métodos privados como `aplicar` e `resposta` para compartilhar o mapeamento dentro da própria classe. Para listas, veja `stream().map(this::resposta).toList()` em `ComodoService`. Exponha DTOs sem senha, hash ou entidades JPA serializadas.

### Service

Use classe concreta com `@Service`, dependências `private final` e construtor explícito, sem interface e implementação separadas ou `@Autowired` em campos. Concentre validação, consultas, atualização e mapeamento no service. Use `jakarta.transaction.Transactional` no método que coordena múltiplas escritas ou exclusões e nas leituras que mapeiam associações LAZY. Com `open-in-view: false`, complete o mapeamento dentro do service. Siga a ordem explícita de exclusão dos registros dependentes, como em `ComodoService.apagar`.

### Controller

Use `@RestController`, construtor explícito e anotações de método HTTP. Siga a rota existente da família. As rotas de domínio usam caminhos como `/casas`, `/comodos`, `/produtos` e `/compras/{compraId}/itens`. A autenticação usa `/user` e saúde usa `/api/health`. O projeto não tem prefixo global `/api`.

Retorne record ou lista diretamente e use `@ResponseStatus` conforme o contrato. A referência usa **200** para consulta, **201** com corpo para criação e **204** com retorno `void` para PATCH e DELETE. Confira o endpoint vizinho, pois ações específicas podem usar outros status. `ResponseEntity` aparece no handler global. Documente controllers com `@Tag(name = "...", description = "...")` em português. `SwaggerConfig` define o requisito global `bearerAuth` e o esquema HTTP Bearer JWT. `@Operation` e `@ApiResponse` não são convenções adotadas no código atual.

## 5. Tratamento de exceções e padrão de resposta

A exceção customizada existente é `exception.RegraNegocioException(String mensagem, HttpStatus status)`. `exception.GlobalExceptionHandler`, anotado com `@RestControllerAdvice`, devolve o status da exceção e o corpo `{"erro":"<mensagem>"}`. Para `DataIntegrityViolationException`, devolve **409** com `{"erro":"Registro duplicado ou vinculado a outros dados"}`.

| Cenário | Tratamento atual |
| --- | --- |
| ID inexistente | `findById(...).orElseThrow(...)` com `RegraNegocioException` e **404** |
| Campo de negócio obrigatório ou valor inválido | `RegraNegocioException` com **400** |
| Duplicidade ou conflito com dados existentes | `RegraNegocioException` com **409** quando identificada pela regra ou tratamento global de integridade. Preserve exceções de contrato, como o **400** de `AuthService.mudarEmail` para e-mail já usado |
| Credenciais locais inválidas | `RegraNegocioException` com **401** |
| Acesso incompatível com uma regra de negócio | `RegraNegocioException` com **403**, como perfil alheio em `ExecucaoLimpezaService` |
| Acesso negado pela política de segurança | Meta-anotação de propriedade ou regra HTTP. A cadeia trata a recusa sem exigir exceção manual do controller |

Os handlers de `SecurityConfig` produzem JSON UTF-8 com a chave `mensagem`. O entry point configurado responde **401** com `{"mensagem":"Acesso não autorizado, faça login para continuar"}` e o access denied handler responde **403** com `{"mensagem":"Você não tem permissão para acessar este recurso"}`. Falhas durante a autenticação do Bearer Token podem ser tratadas pelo próprio Resource Server. O advice atual não define tratamento para toda falha de framework, por exemplo JSON malformado, nem um envelope de sucesso.

## 6. Classes e elementos reutilizáveis

| Componente existente | Onde reutilizar |
| --- | --- |
| `SecurityConfig`, `MethodSecurityConfig`, `JwtService` | Cadeia HTTP, verificação de propriedade, emissão e validação do token local |
| `AuthService`, `UserRepository`, `User` | Fluxos de conta e consultas de usuários. No próprio pacote, `LoginGoogleService` usa `AuthService.senhaCorreta` e `AuthService.iniciarSessao` |
| `LoginGoogleController`, `LoginGoogleService`, `LoginResponse` | Entrada e vínculo de conta Google e contrato de sessão comum |
| `TokenResetService`, `TokenResetRepository`, `TokenReset` | Ciclo do token de recuperação com `criarToken`, `validarToken` e `apagarToken` |
| `EmailService.enviarEmail` | Envio de e-mail pelo `JavaMailSender` já configurado |
| `PerfilService`, `CasaService`, `EnderecoService` e seus repositories | Operações de perfil, casa e endereço. `AuthService` coordena a criação e remoção dos dados da conta e `CasaService` delega operações de endereço |
| `ComprasService`, `EstoqueItemService`, `ComodoService`, `LimpezaService` | Consultas de recursos usadas nas regras de propriedade |
| `ReceitaService.podeLer` e `ReceitaService.proprietario` | Leitura pública/privada e propriedade da receita. Reuse também `ReceitaService.apagarPorUserId` no fluxo existente de remoção da conta |
| Services, repositories e DTOs dos demais pacotes da árvore | Operações e contratos de produtos, itens de compra, ingredientes, favoritos, receitas do usuário e execuções de limpeza. Estenda o pacote responsável pela operação |
| `RegraNegocioException` e `GlobalExceptionHandler` | Falhas de negócio e tradução para o contrato HTTP existente |
| `CorsConfig`, `SwaggerConfig`, `HealthController` e `perfil-padrao.svg` | CORS, documentação OpenAPI, verificação de saúde e imagem padrão de perfil |

O código principal não contém classes base compartilhadas, mappers, helpers globais, enums próprios ou um pacote `utils`. Preserve os valores de negócio existentes, como os estados textuais `fazer`, `fazendo` e `feita` em `ReceitaUsuarioService`. Mantenha helpers locais como `buscar`, `aplicar` e `resposta` no service que os possui.

## 7. Convenções gerais e idioma

Use português no vocabulário do domínio, em métodos de negócio como `cadastrar`, `buscarPorId` e `apagar`, nas mensagens e nas tags e descrições Swagger. Use os sufixos de infraestrutura já presentes em inglês (`Controller`, `Service`, `Repository`, `Request`, `Response`, `Config`) e preserve os prefixos Spring Data (`findBy`, `existsBy`, `deleteBy`). Preserve nomes existentes como `User`, `AuthService`, `userId` e `publica`. As rotas de domínio usam substantivos em português, em minúsculas e com hífen quando necessário, como `/estoque-itens`. Preserve os caminhos existentes em inglês, como `/user/register`, `/user/forgot-password` e `/api/health`.

O histórico usa `develop`, `main` e branches como `feature/casa` e `feature/limpeza`, com exceções como `cozinha`. O formato recorrente de commit é `:sparkles: (Escopo) Descrição em português`, com outros marcadores como `:bug:` e `:rocket:`. São convenções observadas no histórico. O workflow de deploy roda em push para `main` e admite execução manual. [Workflow de deploy](.github/workflows/deploy.yml)

## 8. Exemplo de referência (Golden Path)

Use [`modules.casa.limpeza.comodo`](src/main/java/com/iagora/backend/modules/casa/limpeza/comodo) como referência principal para uma feature CRUD vinculada ao proprietário. A escolha cobre entidade, contrato HTTP, relacionamento LAZY, validação, autorização e exclusão de dependências.

1. Modele o registro como em `Comodo`, com `Integer id`, tabela explícita e vínculo LAZY com a entidade pai.
2. Reproduza a estrutura de `ComodoRepository` e dos records `ComodoRequest` e `ComodoResponse`. Mantenha as consultas derivadas no repository.
3. Implemente o fluxo como em `ComodoService.cadastrar`: valide o vínculo, busque a entidade pai, aplique os campos, persista e construa o response dentro da transação.
4. Espelhe `ComodoController`: o POST verifica propriedade da casa indicada no request, a listagem por casa verifica o ID da casa e GET/PATCH/DELETE individuais verificam o dono do cômodo. Em PATCH, os campos ausentes são preservados e o vínculo com a casa permanece fixo.
5. Para exclusões encadeadas, observe `ComodoService.apagar`, que remove execuções, tarefas e o cômodo nessa ordem, na mesma transação. Use [`LimpezaIntegrationTest`](src/test/java/com/iagora/backend/modules/limpeza/LimpezaIntegrationTest.java) como referência de integração com JWT habilitado e [`ReceitaController`](src/main/java/com/iagora/backend/modules/casa/cozinha/receita/ReceitaController.java) quando houver leitura pública e propriedade no mesmo recurso.

## 9. Regras estritas

- Não adicione dependências, plugins ou alterações de versão sem alinhamento prévio.
- Não reorganize a árvore atual nem introduza camadas, classes base, wrappers, interfaces de service ou pacotes globais por camada.
- Não substitua os records e o mapeamento manual por mappers gerados, serialização de entidades ou DTOs genéricos.
- Não adote anotações Lombok em massa nem troque os construtores explícitos pelo uso de injeção em campos.
- Não altere IDs, tabelas, colunas, relacionamentos ou contratos HTTP existentes para uniformizar diferenças entre módulos.
- Não modifique o Spring Security além do necessário para a regra de uma nova rota. Não crie filtros paralelos, roles implícitas a partir de `User.tipo`, sessões HTTP ou decodificadores de token nos controllers.
- Não amplie a lista pública, inverta a ordem dos matchers ou use IDs vindos do cliente como comprovação de identidade. Preserve a posição exigida por `#p0`.
- Não altere algoritmo, claims, chave ou formato dos JWTs existentes. Não use o ID token do Google como Bearer Token local.
- Não introduza um envelope de resposta ou uma exceção nova para cenários já cobertos por `RegraNegocioException` e pelos handlers atuais.
- Não renomeie rotas e campos JSON para forçar um idioma ou prefixo. Preserve as integrações e restrinja as mudanças à funcionalidade solicitada.
