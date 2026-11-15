# demo-service

App Maven com cerca de 20 dependências de CVEs reais conhecidas, testes e CI no GitHub Actions. Dois papéis: alvo dos PRs draft do vuln fixer e alvo da injeção de falhas do replay (cenários em chaos/).

## Estado

App implementado (commit base dos 20 PRs do vuln fixer). Etapas do plano: P0, P1, P5. Decisões relacionadas: D-29, D-35, D-39.

## Contexto

Este repositório faz parte do laboratório open-eda-labs: workflows agênticos disparados por eventos, com Argo Workflows e padrões abertos. Cada peça trocável é um repositório que só depende do contrato versionado em `triage-contract` (D-01, D-27).

A fonte da verdade é o [Plano Mestre](https://github.com/open-eda-labs/open-eda-labs.github.io/blob/main/docs/PLANO_MESTRE.md).

As regras do laboratório valem para todos os repositórios e estão no [perfil da organização](https://github.com/open-eda-labs).

## Aviso: dependências vulneráveis de propósito

As 20 dependências da [proposta](docs/dependency-proposal.md) estão em versões com CVEs reais, de propósito. Não corrija fora do pipeline do vuln fixer e ignore Dependabot e alertas do GitHub neste repositório. O resultado medido de cada bump está em [docs/bump-matrix.md](docs/bump-matrix.md).

Contenção: o servidor escuta só em `127.0.0.1` por padrão; `/chaos/*` exige JWT HS256 e fica desligado (503) sem segredo; nenhuma entrada HTTP chega crua a uma API vulnerável (o log só recebe a rota conhecida, `log4j2.formatMsgNoLookups=true` e `%m{nolookups}`; `StringSubstitutor` só com `Map`; SnakeYAML lê só `scenarios.yaml` do classpath; XStream só lê o XML que ele mesmo gerou; o lookup `url` aponta só para `banner.txt` do repo; H2 só em memória e sem console; o corpo de `POST /chaos/scenario` tem no máximo 4 KB e o nome é validado). No cluster valem as regras de rede e execução da seção "Contenção" da proposta.

## Construir e rodar

JDK 21 e Maven 3.9:

```sh
mvn -B -ntp verify                     # testes, jar, target/lib e SBOM em target/bom.json
java -jar target/demo-service.jar      # a partir do checkout (app.properties aponta para banner.txt do repo)
CHAOS_JWT_SECRET=<32+ caracteres> ./chaos/run-scenarios.sh
```

O CI roda `mvn -B -ntp verify` e publica o SBOM. Não há scan OSV no CI: o re-scan é do fluxo do vuln fixer.

| Variável | Padrão | Uso |
|---|---|---|
| `PORT` | `8080` | porta HTTP |
| `BIND` | `127.0.0.1` | endereço de escuta |
| `CHAOS_JWT_SECRET` | (nenhum) | segredo HS256 de `/chaos/*`, 32+ caracteres; sem ele `/chaos/*` responde 503 |
| `CHAOS_SEED` | `42` | seed do jitter de latência |
| `INFLUX_URL` | (nenhum) | URL de escrita do InfluxDB (line protocol); sem ela não envia métricas |
| `CHAOS_JWT`, `URL` | (gerado), `http://127.0.0.1:$PORT` | só para `chaos/run-scenarios.sh` |

| Endpoint | O que faz |
|---|---|
| `GET /health` | status, banner e saúde do downstream |
| `GET /work` | chama o downstream simulado via RestTemplate |
| `GET /downstream` | downstream simulado |
| `GET /metrics` | contadores por rota em protobuf `Struct` |
| `GET /debug/scenario?name=` | propriedades do cenário |
| `GET /chaos/scenarios` | nomes dos cenários (JWT) |
| `POST /chaos/scenario` | `{"name": "..."}` executa o cenário (JWT) |

Cenários (latência, erro, OOM simulado com teto de 16 MB) ficam em `src/main/resources/scenarios.yaml`.

## Dependências, uso e testes

| # | Dependência | Classe que usa | Teste que cobre | Classe esperada |
|---|---|---|---|---|
| 1 | log4j-core 2.14.1 (+ log4j-api) | `Routes` | `Log4jCoreTest` | simples |
| 2 | commons-text 1.9 | `chaos.Chaos` | `chaos.CommonsTextTest` | simples |
| 3 | guava 31.1-jre | `chaos.Scenarios` | `chaos.GuavaTest` | simples |
| 4 | commons-io 2.6 | `chaos.Scenarios` | `chaos.CommonsIoTest` | simples |
| 5 | httpclient 4.5.12 | `Downstream` | `HttpClientTest` | simples |
| 6 | nimbus-jose-jwt 9.31 | `Jwt` | `NimbusJoseJwtTest` | simples |
| 7 | org.json 20220320 (profile `chaos`) | `Routes` | `OrgJsonTest` | simples |
| 8 | protobuf-java 3.21.6 | `Metrics` | `ProtobufJavaTest` | simples |
| 9 | commons-lang3 3.17.0 | `chaos.Chaos` | `chaos.CommonsLang3Test` | simples |
| 10 | jackson-databind 2.13.2 (BOM) | `Routes`, `chaos.Scenarios` | `JacksonDatabindTest` | simples |
| 11 | json-smart 2.5.0 (via json-path) | `chaos.Scenarios` | `chaos.JsonSmartTest` | simples |
| 12 | okio 2.8.0 (via okhttp) | `Metrics` | `OkioTest` | simples |
| 13 | snakeyaml 1.33 | `chaos.Scenarios` | `chaos.SnakeYamlTest` | quebra |
| 14 | h2 1.4.200 | `chaos.History` | `chaos.H2Test` | quebra |
| 15 | xstream 1.4.17 | `chaos.Chaos` | `chaos.XStreamTest` | quebra |
| 16 | commons-beanutils 1.9.3 | `chaos.Scenario` | `chaos.CommonsBeanutilsTest` | quebra |
| 17 | spring-web 5.3.39 | `Downstream` | `SpringWebTest` | quebra |
| 18 | commons-configuration2 2.7 | `Config` | `CommonsConfiguration2Test` | quebra |
| 19 | jetty-server 9.4.40 (+ jetty-servlet) | `App`, `Routes` | `AppIntegrationTest` | quebra |
| 20 | commons-compress 1.21 | `chaos.OomFault` | `chaos.CommonsCompressTest` | quebra |
