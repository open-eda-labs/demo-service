package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/** #19 jetty-server/jetty-servlet: integração com o servidor numa porta livre. */
class AppIntegrationTest {

    static final String SECRET = NimbusJoseJwtTest.secret();
    static final HttpClient HTTP = HttpClient.newHttpClient();
    static Server server;
    static String base;

    @BeforeAll
    static void start() throws Exception {
        server = App.start("127.0.0.1", 0, Map.of("CHAOS_JWT_SECRET", SECRET, "CHAOS_SEED", "7"));
        base = "http://127.0.0.1:" + App.port(server);
    }

    @AfterAll
    static void stop() throws Exception {
        server.stop();
    }

    static HttpResponse<String> call(String method, String path, String token, String body) throws Exception {
        HttpRequest.Builder req = HttpRequest.newBuilder(URI.create(base + path))
                .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
        if (token != null) {
            req.header("Authorization", "Bearer " + token);
        }
        return HTTP.send(req.build(), HttpResponse.BodyHandlers.ofString());
    }

    static HttpResponse<String> runScenario(String name) throws Exception {
        return call("POST", "/chaos/scenario", Jwt.sign(SECRET, 60), "{\"name\":\"" + name + "\"}");
    }

    @Test
    void healthWorkEMetricas() throws Exception {
        HttpResponse<String> health = call("GET", "/health", null, null);
        assertEquals(200, health.statusCode());
        assertTrue(health.body().contains("\"downstream\":\"up\""), health.body());
        assertTrue(health.body().contains("vulneráveis de propósito"), health.body());
        HttpResponse<String> work = call("GET", "/work", null, null);
        assertEquals(200, work.statusCode());
        assertTrue(work.body().contains("\"downstream\":\"pronto\""), work.body());
        assertEquals(200, call("GET", "/metrics", null, null).statusCode());
        assertEquals(404, call("GET", "/nada", null, null).statusCode());
    }

    @Test
    void debugScenario() throws Exception {
        HttpResponse<String> debug = call("GET", "/debug/scenario?name=erro-500", null, null);
        assertEquals(200, debug.statusCode());
        assertTrue(debug.body().contains("\"kind\":\"ErrorFault\""), debug.body());
        assertEquals(404, call("GET", "/debug/scenario?name=nao-existe", null, null).statusCode());
        assertEquals(400, call("GET", "/debug/scenario?name=..%2Fx", null, null).statusCode());
    }

    @Test
    void chaosExigeJwt() throws Exception {
        assertEquals(401, call("GET", "/chaos/scenarios", null, null).statusCode());
        assertEquals(401, call("GET", "/chaos/scenarios", Jwt.sign(SECRET, -60), null).statusCode());
        HttpResponse<String> list = call("GET", "/chaos/scenarios", Jwt.sign(SECRET, 60), null);
        assertEquals(200, list.statusCode());
        assertTrue(list.body().contains("\"oom-1mb\""), list.body());
    }

    @Test
    void cenariosInjetamFalhas() throws Exception {
        HttpResponse<String> error = runScenario("erro-500");
        assertEquals(500, error.statusCode());
        assertTrue(error.body().contains("falha injetada no cenário erro-500 com status 500"), error.body());
        assertEquals(200, runScenario("latencia-200ms").statusCode());
        assertEquals(200, runScenario("oom-1mb").statusCode());
        assertEquals(507, runScenario("oom-acima-do-limite").statusCode());
        assertEquals(404, runScenario("nao-existe").statusCode());
        assertEquals(400, call("POST", "/chaos/scenario", Jwt.sign(SECRET, 60), "{").statusCode());
        assertEquals(413, call("POST", "/chaos/scenario", Jwt.sign(SECRET, 60), "x".repeat(5000)).statusCode());
    }

    @Test
    void semSegredoOChaosFicaDesligado() throws Exception {
        Server noSecret = App.start("127.0.0.1", 0, Map.of());
        try {
            HttpResponse<String> resp = HTTP.send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + App.port(noSecret) + "/chaos/scenarios")).build(),
                    HttpResponse.BodyHandlers.ofString());
            assertEquals(503, resp.statusCode());
        } finally {
            noSecret.stop();
        }
    }
}
