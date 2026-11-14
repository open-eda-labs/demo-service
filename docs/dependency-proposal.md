# Proposta: dependências vulneráveis do demo-service

**Status:** proposta para aprovação do Rafa. Nenhum código nem `pom.xml` foi escrito.
**Data da verificação:** 2026-11-14 (dados do OSV mudam; refazer a checagem antes de montar o `pom.xml`).

## Resumo

1. 20 dependências Maven com advisories reais (12 bumps simples, 8 que quebram) e 4 reservas, todas num único serviço HTTP Java 21 sem Spring Boot.
2. Inclui 2 casos transitivos puros (json-smart via json-path, okio via okhttp), 4 casos que exigem mudar de major (snakeyaml, H2, spring-web, Jetty) e 3 casos para estressar o override (versão em property, BOM importado, profile).
3. Todas as 20 linhas e as 4 reservas foram conferidas no OSV e no Maven Central/deps.dev ([F]); o que "quebra" é expectativa de projeto e deve ser confirmado no protótipo.

## Premissas

- **JDK 21 (LTS).** Roda as versões vulneráveis antigas (bytecode Java 8/11) e também os alvos corrigidos que exigem 17+ (Spring 6.2, Jetty 12); suporte longo. (JDK 25 também é LTS; fica como pergunta, não muda a lista.)
- **Sem Spring Boot.** O BOM do Boot (`spring-boot-dependencies`) fixa versões de dezenas de libs ao mesmo tempo: um bump do Boot mexe em jackson, snakeyaml, H2, Jetty, log4j de uma vez, e os 20 PRs deixam de ser independentes. Um app pequeno com versões explícitas por dependência dá um PR por advisory e controle de qual caso usa BOM (só o Jackson, de propósito).
- **Forma do app.** Jetty embarcado (servlets `javax`) expõe `/work`, `/chaos/*` e `/health`; o pacote `chaos` injeta latência, erro e OOM com seed fixa; métricas vão para o InfluxDB por HTTP (line protocol) e daí para o Grafana. Cada dependência tem um uso real coberto por teste (coluna "Uso no app").
- **Base comum.** Cada PR é medido contra o mesmo commit base (todas as 20 vulneráveis). Interações entre PRs estão anotadas.
- **Versão "limpa".** O re-scan reporta todos os advisories do pacote, não só o principal. Por isso a tabela traz a versão mínima que corrige o advisory principal e a mínima que zera todos os advisories OSV do pacote na data da verificação.
- **Legenda.** [F] = advisory, severidade e versões conferidos no OSV e existência das versões conferida no Central (ou deps.dev). [NV] = não verificado. T = transitiva. N1 = `osv-scanner fix` (override) resolve sozinho. N2 = precisa do harness de agente.

## Tabela principal

### Bumps simples (12)

| # | Coordenada | Versão vulnerável | Advisory principal | Sev. | Corrige o principal | Limpa no re-scan (mais recente) | Dir./T | Classe | Nível | Uso no app (o que o teste exercita) | Ver. |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 1 | `org.apache.logging.log4j:log4j-core` | 2.14.1 | CVE-2021-44228 / GHSA-jfh8-c2jp-5v3q | Crítica | 2.15.0 | 2.25.5 (2.26.1) | Direta | Simples; versão em `${log4j2.version}` compartilhada com `log4j-api` (estresse de property) | N1 | Log de cada requisição via log4j-api; teste lê a linha num appender em memória | [F] |
| 2 | `org.apache.commons:commons-text` | 1.9 | CVE-2022-42889 / GHSA-599f-7c49-w659 | Crítica | 1.10.0 | 1.10.0 (1.15.0) | Direta | Simples | N1 | `StringSubstitutor` com `Map` (sem interpolador de lookups) monta a mensagem de erro injetada | [F] |
| 3 | `com.google.guava:guava` | 31.1-jre | CVE-2023-2976 / GHSA-7g45-4rm6-3mm3 | Moderada | 32.0.0-jre | 33.7.2-jre (33.8.0-jre) | Direta | Simples | N1 | `CacheBuilder` com `Ticker` falso guarda cenários carregados; teste de expiração | [F] |
| 4 | `commons-io:commons-io` | 2.6 | CVE-2021-29425 / GHSA-gwrp-pvrq-jmwv | Moderada | 2.7 | 2.14.0 | Direta | Simples | N1 | `FilenameUtils.normalize` e `IOUtils` ao ler arquivos de cenário; teste com caminhos relativos | [F] |
| 5 | `org.apache.httpcomponents:httpclient` | 4.5.12 | CVE-2020-13956 / GHSA-7r82-7xv7-xcpj | Moderada | 4.5.13 | 4.5.13 (4.5.14) | Direta | Simples | N1 | Health-check do downstream simulado; teste contra servidor local | [F] |
| 6 | `com.nimbusds:nimbus-jose-jwt` | 9.31 | CVE-2023-52428 / GHSA-gvpg-vgmx-xg6w | Alta | 9.37.2 | 9.37.4 (10.10) | Direta | Simples | N1 | Assina e valida JWT HS256 que protege `/chaos/*`; teste de token válido e expirado | [F] |
| 7 | `org.json:json` | 20220320 | CVE-2023-5072 / GHSA-4jq9-2xhw-jpx7 | Alta | 20231013 | 20260719 (20260814) | Direta, **só no profile `chaos`** (`activeByDefault`) | Simples; estresse de profile | N1 esperado, N2 se o override ignorar o profile | Faz o parse do corpo JSON de `POST /chaos/scenario`; teste com payload válido | [F] |
| 8 | `com.google.protobuf:protobuf-java` | 3.21.6 | CVE-2022-3171 / GHSA-h4h5-3hr4-j3g2 | Moderada | 3.21.7 | 3.25.5 | Direta | Simples (limpo exige 3.25.5 por CVE-2024-7254) | N1 | Serializa snapshot de métricas em `Struct`; teste de ida e volta em bytes | [F] |
| 9 | `org.apache.commons:commons-lang3` | 3.17.0 | CVE-2025-48924 / GHSA-j288-q9x7-2f5v | Moderada | 3.18.0 | 3.18.0 (3.22.0) | Direta | Simples | N1 | `Validate`/`StringUtils` nos parâmetros de `/chaos`; `ClassUtils.getClass` resolve a estratégia de falha pelo nome | [F] |
| 10 | `com.fasterxml.jackson.core:jackson-databind` | 2.13.2 (via `jackson-bom` importado, `${jackson.version}`) | CVE-2022-42003 / GHSA-jjjh-jjxp-wpff | Alta | 2.13.4.2 | 2.18.11 (2.23.0) | Direta, versão vinda do BOM | Simples no código; estresse de BOM (ver seção abaixo) | N1 esperado; N2 provável | Serializa as respostas JSON; teste de contrato do corpo | [F] |
| 11 | `net.minidev:json-smart` | 2.5.0 | CVE-2024-57699 / GHSA-pq2g-wx69-c263 | Alta | 2.5.2 | 2.5.2 (2.6.1) | **T**, via `com.jayway.jsonpath:json-path` 2.9.0 (sem advisory próprio) | Simples; override de transitiva | N1 | `JsonPath.read` extrai campos dos cenários; teste com expressões | [F] |
| 12 | `com.squareup.okio:okio` | 2.8.0 | CVE-2023-3635 / GHSA-w33c-445m-f8w7 | Moderada | 3.4.0 | 3.4.0 (3.18.2) | **T**, via `com.squareup.okhttp3:okhttp` 4.9.3 (sem advisory próprio) | Simples; override de transitiva que muda de major (okio 2 para 3, compatível binário na JVM, a confirmar) | N1 | okhttp envia métricas em line protocol para o InfluxDB; teste contra stub HTTP local | [F] |

### Bumps que quebram (8)

| # | Coordenada | Versão vulnerável | Advisory principal | Sev. | Corrige o principal | Limpa no re-scan (mais recente) | Dir./T | O que quebra | Nível | Uso no app (o que o teste exercita) | Ver. |
|---|---|---|---|---|---|---|---|---|---|---|---|
| 13 | `org.yaml:snakeyaml` | 1.33 | CVE-2022-1471 / GHSA-mjmj-j48q-9wg2 | Alta | 2.0 | 2.0 (2.7) | Direta | **Major 1 para 2, API removida:** `new Constructor(Class)` passa a exigir `LoaderOptions`; erro de compilação | N2 | `new Yaml(new Constructor(Scenario.class))` carrega `scenarios.yaml` | [F] |
| 14 | `com.h2database:h2` | 1.4.200 | CVE-2021-42392 / GHSA-h376-j262-vhq6 | Crítica | 2.0.206 | 2.2.220 (2.5.252) | Direta | **Major 1 para 2, comportamento:** palavras reservadas novas (ex.: coluna `value`) e regras de SQL mais estritas; falha nos testes de repositório | N2 | Histórico de falhas injetadas em H2 em memória (tabela com colunas `key`/`value`); teste de insert e consulta | [F] |
| 15 | `com.thoughtworks.xstream:xstream` | 1.4.17 | CVE-2021-39139 / GHSA-64xx-cq4q-mf44 | Alta | 1.4.18 | 1.4.21 (1.4.21) | Direta | **Comportamento default:** a partir de 1.4.18 o security framework só aceita tipos permitidos; `fromXML` de POJO próprio lança `ForbiddenClassException` até chamar `allowTypes` | N2 | Exporta e importa snapshot de cenário em XML; teste de ida e volta | [F] |
| 16 | `commons-beanutils:commons-beanutils` | 1.9.3 | CVE-2019-10086 / GHSA-6phf-73q6-gh87 | Alta | 1.9.4 | 1.11.0 | Direta | **Comportamento default:** 1.9.4 suprime a propriedade `class` por padrão; `BeanUtils.describe` deixa de devolvê-la | N2 | `/debug/scenario` usa `BeanUtils.describe`; teste compara o conjunto de chaves | [F] |
| 17 | `org.springframework:spring-web` | 5.3.39 | CVE-2016-1000027 / GHSA-4wrc-f8pq-fpqp | Crítica | 6.0.0 | 6.2.19 (7.0.10) | Direta (+ `spring-core` T com CVE-2025-41249, sem correção OSS em 5.3/6.1) | **Major 5 para 6:** `HttpComponentsClientHttpRequestFactory` passa a exigir Apache HttpClient 5; código que passa um cliente HttpClient 4 não compila. Exige incluir `httpclient5` (limpo em 5.6.3) | N2 | `RestTemplate` sobre HttpClient 4 chama o downstream simulado em `/work` | [F] |
| 18 | `org.apache.commons:commons-configuration2` | 2.7 | CVE-2022-33980 / GHSA-xj57-8qj4-c4m6 | Crítica | 2.8.0 | 2.15.0 (2.15.1) | Direta | **Comportamento default:** 2.8.0 desliga os lookups `script`, `dns` e `url`; `${url:...}` deixa de ser resolvido e o teste de config falha | N2 | `app.properties` inclui um valor via `${url:UTF-8:file:...}` apontando para arquivo local do repo | [F] |
| 19 | `org.eclipse.jetty:jetty-server` (+ `jetty-servlet`) | 9.4.40.v20210413 | CVE-2026-6790 / GHSA-7p3p-8qv8-m2vh (jetty-server); CVE-2024-6763 / GHSA-qh8g-58pp-2wxh (jetty-http, T) | Moderada | 12.0.35 / 12.0.12 (sem correção na linha 9.4) | 12.0.36 (12.1.15) | Direta (+ `jetty-http`, `jetty-security` T) | **Major 9 para 12 e `javax` para `jakarta`:** `jetty-servlet` vira `jetty-ee10-servlet`, `org.eclipse.jetty.servlet` vira `org.eclipse.jetty.ee10.servlet`, servlets migram para `jakarta.servlet`. É o caso mais pesado | N2 | Servidor HTTP do app (`ServletContextHandler` + `HttpServlet`); teste de integração sobe o servidor numa porta livre | [F] |
| 20 | `org.apache.commons:commons-compress` | 1.21 | CVE-2024-25710 / GHSA-4g9r-vxhx-9pgx | Moderada | 1.26.0 | 1.26.0 (1.28.0) | Direta | **Conflito de transitiva:** 1.26.0 depende de commons-io 2.15.1, mas o `commons-io` 2.6 direto (#4) vence na mediação; esperado `NoClassDefFoundError`/`NoSuchMethodError` em runtime (a confirmar no protótipo). Correção: subir também o commons-io | N2 | Cenário OOM descompacta payload gerado em memória (`ZipArchiveInputStream`/`ZipFile`); teste de ida e volta | [F] |

Contagem: 12 simples e 8 que quebram; transitivos puros: #11 e #12 (mais as transitivas que acompanham #17 e #19); mudança de major: #13, #14, #17, #19 (e #12 na transitiva).

### Advisories extras que vão aparecer no scan

O scan vai reportar também pacotes que vêm junto com as linhas acima. Eles somem quando a linha "dona" é corrigida até a versão limpa, mas geram alertas próprios:

| Pacote | Versão | Advisories (exemplos) | Vem de | Ver. |
|---|---|---|---|---|
| `org.apache.logging.log4j:log4j-api` | 2.14.1 | CVE-2026-49844 / GHSA-qv9r-c865-cp47 (corrige em 2.25.5) | #1 (mesma property) | [F] |
| `com.fasterxml.jackson.core:jackson-core` | 2.13.2 | CVE-2025-52999 / GHSA-h46c-h94j-95f3 e outros (limpa em 2.18.11) | #10 (mesmo BOM) | [F] |
| `org.springframework:spring-core` | 5.3.39 | CVE-2025-41249 / GHSA-jmp9-x22r-554x (limpa em 6.2.19) | #17 | [F] |
| `org.eclipse.jetty:jetty-http`, `jetty-security` | 9.4.40.v20210413 | CVE-2024-6763, CVE-2026-10050 e outros | #19 | [F] |
| `org.jetbrains.kotlin:kotlin-stdlib` | 1.4.10 | CVE-2022-24329 / GHSA-2qp4-g3q3-f92w (corrige em 1.6.0) | okhttp 4.9.3 (ver reserva R1) | [F] |

## Reservas

| # | Coordenada | Versão vulnerável | Advisory principal | Sev. | Corrige o principal | Limpa no re-scan (mais recente) | Dir./T | Classe | Nível | Uso no app | Ver. |
|---|---|---|---|---|---|---|---|---|---|---|---|
| R1 | `org.jetbrains.kotlin:kotlin-stdlib` | 1.4.10 | CVE-2022-24329 / GHSA-2qp4-g3q3-f92w | Moderada | 1.6.0 | 1.6.0 (2.4.21) | T, via okhttp 4.9.3 | Simples; já vem com #12 sem custo | N1 | Mesmo cliente okhttp do #12 | [F] |
| R2 | `com.google.code.gson:gson` | 2.8.8 | CVE-2022-25647 / GHSA-4jrv-ppp4-jm57 | Alta | 2.8.9 | 2.8.9 (2.15.0) | Direta | Simples | N1 | Lê o arquivo de baseline de métricas | [F] |
| R3 | `org.jsoup:jsoup` | 1.14.2 | CVE-2022-36033 / GHSA-gp7f-rwcx-9369 | Moderada | 1.15.3 | 1.23.2 (1.24.1) | Direta | Provável quebra se usar `Whitelist` (renomeada para `Safelist`) [NV quanto à versão exata da remoção] | N1 ou N2 | Sanitiza a descrição HTML do cenário | [F] |
| R4 | `commons-collections:commons-collections` | 3.2.1 | CVE-2015-7501 / GHSA-fjq5-5j5f-mvxh | Crítica | 3.2.2 | 3.2.2 | Direta | Simples | N1 | `LRUMap` para últimas N falhas; nunca desserializar objetos | [F] |

**Excluídos por não ter versão corrigida na mesma coordenada (seriam N3 ou troca de artefato):**

- `log4j:log4j` 1.2.17: CVE-2019-17571, CVE-2021-4104 e outros sem correção na linha 1.x [F].
- `org.codehaus.jackson:jackson-mapper-asl` 1.9.13: CVE-2019-10172, CVE-2019-10202 sem correção [F].
- `org.apache.velocity:velocity` 1.7: CVE-2020-13936 sem correção nesse artefato (só em `velocity-engine-core`) [F].
- `org.bouncycastle:bcprov-jdk15on` 1.70: CVE-2023-33202 / GHSA-wjxj-5m7g-mg7q sem correção no artefato `jdk15on` (só em `bcprov-jdk18on`) [F]. Bom candidato futuro para "troca de coordenada", fora do escopo N1/N2.

## Casos que estressam o override

O plano registra que o override do `osv-scanner fix` é experimental e tem limitações com properties em `groupId`/`artifactId` e com profiles. Casos propostos:

1. **Versão em property compartilhada (#1, log4j).** `log4j-api` e `log4j-core` usam `${log4j2.version}`. O override precisa subir a property (ou criar override só para o core, deixando o api vulnerável e desalinhado). Mede se o N1 produz um par alinhado e limpo (2.25.5 nos dois).
2. **BOM importado em `dependencyManagement` (#10, Jackson).** `jackson-bom` 2.13.2 importado com `${jackson.version}`. Se o override subir só o `jackson-databind` para 2.18.11 e deixar `jackson-core` 2.13.2 do BOM, o esperado é falha de runtime por versões desalinhadas (e `jackson-core` continua no re-scan). Correção certa: subir a property do BOM. Esperado: N1 falha, N2 resolve.
3. **Dependência só num profile (#7, org.json).** Declarada em `<profiles><profile id="chaos">` com `activeByDefault`. Mede se o override enxerga e altera o profile; se não, o re-scan ainda acusa e cai no N2.
4. **Opcional, não proposto por padrão:** `groupId` ou `artifactId` definido por property (ex.: `${guava.artifact}`). É a limitação documentada mais direta, mas não é um padrão realista de `pom.xml`. Ver pergunta 4.

Interações que também estressam o pipeline (não o override em si): #20 depende de #4 (commons-io), #17 exige adicionar uma dependência nova (`httpclient5`), #19 muda artefato e pacote.

## Contenção

O app carrega libs vulneráveis de propósito. Regras mínimas:

- **Rede:** só `ClusterIP`, sem Ingress, sem NodePort, sem port-forward permanente. NetworkPolicy de egress negando tudo exceto o InfluxDB do laboratório (bloqueia callbacks JNDI/LDAP e downloads remotos).
- **Dados:** nenhum dado real, nenhum segredo de produção; JWT com chave só do laboratório; H2 só em memória.
- **Caminhos exploráveis desligados quando possível:**
  - Log4j: `-Dlog4j2.formatMsgNoLookups=true`, nenhum lookup (`${jndi:}`, `${ctx:}`, `${env:}`) nos patterns e nenhum dado de requisição em pattern com lookup.
  - commons-text: só `StringSubstitutor` com `Map`; nunca `createInterpolator()`.
  - snakeyaml e XStream: só leem arquivos do próprio repo, nunca corpo de requisição.
  - commons-configuration2: o lookup `url` só aponta para arquivo local fixo; nada de `script`/`dns`.
  - commons-collections/beanutils (se usados): sem desserialização Java nativa em lugar nenhum.
  - Spring: não usar `HttpInvoker`/remoting (é o caminho do CVE-2016-1000027).
- **Execução:** container sem root, filesystem somente leitura (exceto `/tmp`), limites de CPU/memória (também necessário para o cenário OOM).
- **Repositório:** sem publicar imagem em registry público; Dependabot/alertas do GitHub conscientemente ignorados neste repo (documentar no README para ninguém "corrigir" fora do pipeline).

## Perguntas para o Rafa

1. **Major bump é N2 ou N3?** O plano define N3 como "sem versão corrigida na mesma linha". Por essa definição, #13 (snakeyaml), #14 (H2), #17 (spring-web) e #19 (Jetty) seriam N3. A proposta trata como N2 (migração, não backport). Se for N3, trocar esses 4 por reservas e por casos de quebra dentro da linha.
2. **Critério do re-scan:** some só o advisory alvo do PR, ou todos os advisories do pacote? Muda a versão-alvo (ex.: log4j 2.15.0 contra 2.25.5; Jetty 9.4.57 corrige CVE-2024-13009, mas limpo só em 12.0.36).
3. **Família = 1 PR?** `log4j-api`, `jackson-core`, `spring-core`, `jetty-http`/`jetty-security` e `kotlin-stdlib` vão gerar alertas próprios. Proposta: agrupar na linha "dona" e contar 1 PR. Alternativa: contar à parte e reduzir a lista.
4. **Incluir um caso artificial de `groupId`/`artifactId` em property** para testar a limitação documentada? Proposta: não, a menos que o gate precise dele.
5. **Jetty como servidor do app (#19)?** É o caso mais pesado e afeta o papel de injeção de falhas. Alternativa: servidor com `com.sun.net.httpserver` do JDK (sem dependência) e Jetty sai da lista (entra R2 ou R3).
6. **JDK 21 ou 25?** Proposta: 21. Ambos servem; 25 aumenta o risco com libs antigas (XStream 1.4.17, H2 1.4.200).
7. **Quantas falhas cabem no gate?** Com 8 casos N2, o gate de 80% (16 de 20) exige que o N2 resolva pelo menos 4 dos 8. Aceitar essa proporção ou reduzir para 6 quebras?
8. **Ordem e base:** confirmar que cada PR é medido contra o mesmo commit base (todas vulneráveis), sem empilhar PRs.

## Como foi verificado

- **OSV:** `POST https://api.osv.dev/v1/query` e `/v1/querybatch` para cada coordenada e versão vulnerável, e para cada versão-alvo (sem advisories na data). Severidade = campo `database_specific.severity` do advisory GitHub. Versões "corrige o principal" e "limpa no re-scan" foram calculadas varrendo todas as versões estáveis posteriores no OSV.
- **Existência das versões:** POM de cada versão vulnerável conferido em `repo1.maven.org` ou `repo.maven.apache.org` (mesmo Maven Central). Versões-alvo e "mais recente" conferidas via `api.deps.dev` (índice do Maven Central).
- **Árvores transitivas:** `api.deps.dev` (`:dependencies`) para json-path 2.9.0, okhttp 4.9.3, commons-compress 1.26.0, spring-web 5.3.39/6.2.19, commons-beanutils 1.9.3, commons-configuration2 2.7 e Jetty 9.4.40; o escopo `provided` do `servlet-api` em commons-configuration2 2.7 foi conferido no POM.
- **Rede:** nenhum host foi bloqueado pelo proxy. `repo1.maven.org` aplicou rate limit (HTTP 429 e 404 com `retry-after` em `maven-metadata.xml`); a verificação usou `repo.maven.apache.org` e `api.deps.dev` no lugar, sem desabilitar TLS.
- **O que não foi verificado:** o comportamento de quebra (coluna "O que quebra") é expectativa baseada nas notas de versão conhecidas; não houve build. Os pontos marcados "a confirmar" (#12 okio 3 sob okhttp 4.9, #20 conflito commons-io) e a versão de remoção do `Whitelist` no jsoup (R3) devem ser confirmados no protótipo antes de fechar a lista.
