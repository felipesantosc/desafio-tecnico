# Coupon API — Desafio Técnico

API REST para cadastro, consulta e remoção (soft delete) de cupons de desconto, construída com **contract first** (OpenAPI), **arquitetura hexagonal** e regras de negócio **encapsuladas em objetos de domínio**.

| | |
|---|---|
| **Stack** | Java 21 · Spring Boot 4.1 · Spring Data JPA · H2 (em memória) · OpenAPI Generator · springdoc (Swagger UI) |
| **Testes** | JUnit 5 · AssertJ · MockMvc · JaCoCo (mínimo de 80% de linhas e branches, verificado no build) |
| **Infra** | Docker (build multi-stage) · Docker Compose |

---

## Como rodar

### Com Docker (recomendado — não precisa de Java instalado)

```bash
docker compose up --build
```

O build da imagem **executa todos os testes e a verificação de cobertura**; se algo falhar, a imagem não é gerada. Para um build rápido sem testes, altere `SKIP_TESTS` para `"true"` no `docker-compose.yml`.

### Com Maven (requer Java 21)

```bash
./mvnw spring-boot:run
```

### Endereços

| Recurso | URL |
|---|---|
| API | http://localhost:8080/coupon |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Contrato OpenAPI | http://localhost:8080/v3/api-docs.yaml |
| Console H2 | http://localhost:8080/h2-console — JDBC URL `jdbc:h2:mem:desafio`, usuário `sa`, senha em branco |

### Testes e cobertura

```bash
./mvnw clean verify
```

Executa os 86 testes e falha se a cobertura ficar abaixo de 80%. Relatório em `target/site/jacoco/index.html`.

---

## Endpoints

| Método | Rota | Sucesso | Erros |
|---|---|---|---|
| `POST` | `/coupon` | `201` cupom criado | `400` regra violada ou JSON inválido |
| `GET` | `/coupon/{id}` | `200` cupom | `400` id não é UUID · `404` não encontrado |
| `DELETE` | `/coupon/{id}` | `204` removido (soft delete) | `400` id não é UUID · `404` não encontrado · `409` já removido |

Todos os erros seguem o mesmo formato: `{ "status": 400, "message": "...", "timestamp": "..." }`.

```bash
curl -X POST http://localhost:8080/coupon \
  -H "Content-Type: application/json" \
  -d '{"code":"ABC-123","description":"Cupom de boas-vindas","discountValue":0.8,"expirationDate":"2030-12-31T23:59:59Z","published":false}'
```

```json
{"id":"…","code":"ABC123","description":"Cupom de boas-vindas","discountValue":0.8,"expirationDate":"2030-12-31T23:59:59Z","status":"ACTIVE","published":false,"redeemed":false}
```

---

## Regras de negócio: onde estão e como são testadas

| Regra do enunciado | Implementação | Testes |
|---|---|---|
| Campos obrigatórios: `code`, `description`, `discountValue`, `expirationDate` | Contrato (`required`) e `Coupon.create` | `CouponTest`, `CouponApiIntegrationTest` |
| Código alfanumérico com 6 caracteres; especiais aceitos na entrada e removidos antes de salvar e responder | Value Object `CouponCode` | `CouponCodeTest`, `CouponApiIntegrationTest` |
| Desconto mínimo de 0,5, sem máximo | Value Object `DiscountValue` (e `minimum` no contrato) | `DiscountValueTest`, `CouponApiIntegrationTest` |
| Expiração nunca no passado | `Coupon.create`, com `Clock` injetado | `CouponTest` (inclui a borda exata e fusos diferentes) |
| Pode ser criado já publicado (`published` opcional, padrão `false`) | `Coupon.create` / `CouponWebMapper` | `CouponTest`, `CouponApiIntegrationTest` |
| Delete é soft delete, sem perda dos dados do cadastro | `Coupon.delete()` altera apenas o `status`; a porta de repositório não expõe remoção física | `CouponTest`, `DeleteCouponUseCaseTest`, `CouponApiIntegrationTest` |
| Não é possível deletar um cupom já deletado | `Coupon.delete()` + lock otimista (`@Version`) para requisições simultâneas | `CouponTest`, `DeleteCouponUseCaseTest`, `CouponRepositoryAdapterIntegrationTest` |

---

## Arquitetura

Arquitetura hexagonal (*ports & adapters*). As dependências apontam sempre para dentro: o domínio não conhece ninguém, a aplicação conhece apenas o domínio e a infraestrutura conhece ambos.

```
infrastructure ──► application ──► domain
 (adapters)        (casos de uso)   (regras)
```

```
src/main/java/com/teste/desafio_tecnico
├── domain                         Java puro, sem Spring nem JPA
│   ├── model                      Coupon (entidade), CouponCode e DiscountValue (Value Objects), CouponStatus
│   ├── exception                  exceções de negócio
│   └── repository                 CouponRepository (porta de saída)
├── application
│   └── usecase                    CreateCouponUseCase, GetCouponUseCase, DeleteCouponUseCase, CreateCouponCommand
└── infrastructure
    ├── web                        CouponController, mappers, tratamento de erros
    │   └── api                    (gerado no build a partir do openapi.yaml)
    ├── persistence                CouponEntity (JPA), adapter da porta, mapper
    └── config                     registro dos casos de uso e do Clock no Spring
```

Fluxo de um `DELETE /coupon/{id}`:

```
CouponController ─► DeleteCouponUseCase.execute(id)
                        ├─ couponRepository.findById(id)     porta → CouponRepositoryAdapter → JPA
                        ├─ coupon.delete()                   regra no domínio
                        └─ couponRepository.save(coupon)     @Version detecta escrita concorrente
```

---

## Decisões técnicas

**Contract first.** O `src/main/resources/openapi/openapi.yaml` é a fonte da verdade. O OpenAPI Generator gera no build a interface `CouponApi` e os DTOs, que o `CouponController` implementa; qualquer divergência entre contrato e código vira erro de compilação. O Swagger UI é montado pelo springdoc a partir das anotações que o próprio gerador coloca na `CouponApi`, então também reflete o contrato.

**Regras no domínio, não no caso de uso.** O caso de uso apenas orquestra (busca, delega e salva). As validações ficam na entidade e nos Value Objects. O `Coupon` não tem setters, só é criado por `Coupon.create(...)` e todos os seus campos são `final`, exceto o `status`, que muda apenas por `delete()`.

**Criar vs. reconstituir.** `Coupon.create` aplica as regras de criação; `Coupon.restore` reconstitui um cupom vindo do banco. Assim, um cupom que expirou depois de criado continua legível.

**Validação em camadas.** O contrato valida o formato da requisição HTTP; o domínio garante as regras para qualquer ponto de entrada. Exemplo: uma descrição só com espaços passa pelo `minLength` do contrato, mas é barrada pelo domínio.

**Casos de uso sem framework.** São classes Java puras, com um único método `execute`, registradas no Spring pela `UseCaseConfig`. Recebem um `CreateCouponCommand`, nunca o DTO HTTP, e não dependem de web nem de JPA.

**Modelo de domínio separado do modelo de persistência.** `Coupon` (domínio) e `CouponEntity` (JPA) são classes distintas, ligadas por um mapper. O domínio não precisa de construtor vazio nem de anotações de banco.

**Concorrência no delete.** Sem transação na camada de aplicação, duas requisições simultâneas poderiam ler o cupom como `ACTIVE` e ambas removê-lo. O `@Version` (lock otimista) faz a segunda escrita falhar, e o adapter a traduz para `409`. O domínio carrega a versão como um token opaco, uma concessão consciente para manter a aplicação livre de transações.

**Tempo injetável.** O `Clock` é injetado, então os testes fixam o "agora" e verificam bordas exatas (expiração igual ao momento atual, um segundo antes, outro fuso).

**Erros previsíveis.** Todo erro, seja de negócio, validação, JSON malformado ou do próprio Spring (405, 415), sai no formato `ErrorResponse` do contrato. Erros inesperados retornam `500` com mensagem genérica, sem expor detalhes internos. O locale é fixo em `pt_BR`, para que as mensagens não variem com o `Accept-Language` ou com o sistema operacional do container.

**Precisão do desconto.** A coluna usa escala 4 e os zeros à direita são removidos na leitura, para que o valor retornado seja o mesmo enviado (`0.8` e não `0.80`).

---

## Estratégia de testes

| Nível | Escopo | Abordagem |
|---|---|---|
| Domínio | `Coupon`, `CouponCode`, `DiscountValue` | JUnit puro, `Clock` fixo, testes parametrizados para as bordas |
| Aplicação | Casos de uso | Repositório **fake** em memória que guarda cópias, como um banco: detecta, por exemplo, uma alteração não salva |
| Integração | API e persistência | `@SpringBootTest` + MockMvc + H2 real, tentando violar cada regra pelo HTTP; lock otimista simulado de forma determinística |

Mocks são usados apenas para simular uma falha inesperada (teste do `500`). As regras de negócio são sempre exercitadas com objetos reais.

---

## Fora do escopo / próximos passos

- **Unicidade do `code`**: o enunciado não exige; seria uma regra de domínio com índice único no banco.
- **Banco persistente**: trocar H2 por PostgreSQL afeta apenas a configuração e a infraestrutura.
- **Migrações versionadas** (Flyway) em vez de `ddl-auto`.
- **Observabilidade**: Actuator com health check no `docker-compose.yml`.
