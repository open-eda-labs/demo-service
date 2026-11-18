# Matriz de bumps medida

> Matriz histórica: foi medida antes da divisão em `demo-parent` e `demo-lib` (2026-11-15) e com a menor versão corrigida.

**Data:** 2026-11-15. Cada linha é uma cópia do commit base com só o bump daquela linha para a versão "Corrige o principal" da [proposta](dependency-proposal.md), seguida de `mvn -B -ntp -q verify` (JDK 21, Maven 3.9.11).

Como cada bump foi aplicado: versão direta trocada no `pom.xml`; #1 pela property `log4j2.version` (log4j-api e log4j-core juntos); #10 pela property `jackson.version` do `jackson-bom` (2.13.4.20221013 é o BOM que traz jackson-databind 2.13.4.2 e jackson-core 2.13.4); #7 dentro do profile `chaos`; #11 e #12 por `dependencyManagement`; #19 só `jetty-server` (não existe `jetty-servlet` 12; a troca para `jetty-ee10-servlet` faz parte da migração N2).

Resultado: 20 de 20 batem com a proposta (12 verdes, 8 falhas).

| # | Dependência | De | Para | Resultado real | Erro resumido | Bate com a proposta |
|---|---|---|---|---|---|---|
| 1 | log4j-core (+ log4j-api) | 2.14.1 | 2.15.0 | verde | | sim |
| 2 | commons-text | 1.9 | 1.10.0 | verde | | sim |
| 3 | guava | 31.1-jre | 32.0.0-jre | verde | | sim |
| 4 | commons-io | 2.6 | 2.7 | verde | | sim |
| 5 | httpclient | 4.5.12 | 4.5.13 | verde | | sim |
| 6 | nimbus-jose-jwt | 9.31 | 9.37.2 | verde | | sim |
| 7 | org.json (profile `chaos`) | 20220320 | 20231013 | verde | | sim |
| 8 | protobuf-java | 3.21.6 | 3.21.7 | verde | | sim |
| 9 | commons-lang3 | 3.17.0 | 3.18.0 | verde | | sim |
| 10 | jackson-databind (BOM) | 2.13.2 | 2.13.4.2 (BOM 2.13.4.20221013) | verde | | sim |
| 11 | json-smart (T via json-path) | 2.5.0 | 2.5.2 | verde | | sim |
| 12 | okio (T via okhttp 4.9.3) | 2.8.0 | 3.4.0 | verde | okio-jvm 3.4.0 entra; kotlin-stdlib 1.4.10 continua vencendo a mediação e nada quebra | sim |
| 13 | snakeyaml | 1.33 | 2.0 | falha (compilação) | `Scenarios.java`: `Class<Scenario> cannot be converted to LoaderOptions` | sim |
| 14 | h2 | 1.4.200 | 2.0.206 | falha (teste) | `H2Test`: `Syntax error in SQL statement "CREATE TABLE FAULT_LOG (KEY[*] ...` (`key` reservada) | sim |
| 15 | xstream | 1.4.17 | 1.4.18 | falha (teste) | `XStreamTest` e `AppIntegrationTest`: `ForbiddenClassException: labs.demo.lib.chaos.Scenario` | sim |
| 16 | commons-beanutils | 1.9.3 | 1.9.4 | falha (teste) | `CommonsBeanutilsTest`: chave `class` some do `describe` | sim |
| 17 | spring-web | 5.3.39 | 6.0.0 | falha (compilação) | `Downstream.java`: `cannot access org.apache.hc.client5.http.classic.HttpClient` | sim |
| 18 | commons-configuration2 | 2.7 | 2.8.0 | falha (teste) | `CommonsConfiguration2Test` e `AppIntegrationTest`: `${url:UTF-8:file:...}` volta sem resolver | sim |
| 19 | jetty-server | 9.4.40.v20210413 | 12.0.35 | falha (compilação) | `Routes.java`: `package javax.servlet.http does not exist` | sim |
| 20 | commons-compress | 1.21 | 1.26.0 | falha (teste) | `CommonsCompressTest`: `NoClassDefFoundError: org/apache/commons/io/build/AbstractStreamBuilder` (commons-io 2.6 vence a mediação) | sim |

Notas do protótipo:

- #20: com `ZipArchiveInputStream`/`ZipArchiveOutputStream` sozinhos o bump ficou verde. O app passou a ler o payload com `ZipFile` sobre `SeekableInMemoryByteChannel` (uso citado na proposta), e aí o bump quebra. Com commons-compress 1.26.0 e commons-io 2.15.1 juntos o build fica verde.
- #12: confirmado como simples (okio 3.4.0 sob okhttp 4.9.3).
