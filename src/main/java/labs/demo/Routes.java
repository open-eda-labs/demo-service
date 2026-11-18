package labs.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Ticker;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import labs.demo.lib.Config;
import labs.demo.lib.Downstream;
import labs.demo.lib.Jwt;
import labs.demo.lib.Metrics;
import labs.demo.lib.chaos.Chaos;
import labs.demo.lib.chaos.History;
import labs.demo.lib.chaos.Scenario;
import labs.demo.lib.chaos.Scenarios;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/** Todas as rotas HTTP num servlet só. */
final class Routes extends HttpServlet {

    static final ObjectMapper JSON = new ObjectMapper();
    private static final Logger LOG = LogManager.getLogger(Routes.class);
    private static final int MAX_BODY = 4096;
    private static final Set<String> ROUTES = Set.of("GET /health", "GET /work", "GET /downstream", "GET /metrics",
            "GET /debug/scenario", "GET /chaos/scenarios", "POST /chaos/scenario");

    record Health(String status, String service, String banner, String downstream) {}

    private final String bind;
    private final String jwtSecret;
    private final PropertiesConfiguration config = Config.load();
    private final Scenarios scenarios = new Scenarios(Ticker.systemTicker());
    private final Downstream downstream = new Downstream();
    private final Chaos chaos;
    private final Metrics metrics;

    Routes(String bind, Map<String, String> env) {
        this.bind = bind;
        this.jwtSecret = env.get("CHAOS_JWT_SECRET");
        this.chaos = new Chaos(scenarios, new History(), Long.parseLong(env.getOrDefault("CHAOS_SEED", "42")));
        this.metrics = new Metrics(env.get("INFLUX_URL"), env.get("INFLUX_TOKEN"));
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        long start = System.nanoTime();
        String key = req.getMethod() + " " + req.getPathInfo();
        String route = ROUTES.contains(key) ? key : "desconhecida";
        int status;
        try {
            status = handle(route, req, resp);
        } catch (IllegalArgumentException | JSONException e) {
            status = send(resp, 400, Map.of("erro", "requisição inválida"));
        }
        long millis = (System.nanoTime() - start) / 1_000_000;
        // Só a rota conhecida (constante) vai para o log: nenhuma entrada HTTP crua chega ao Log4j.
        LOG.info("requisicao rota={} status={} duracao_ms={}", route, status, millis);
        metrics.record(route, status, millis);
    }

    private int handle(String route, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String self = "http://" + bind + ":" + req.getLocalPort();
        switch (route) {
            case "GET /health":
                return send(resp, 200, new Health("ok", config.getString("service.name"),
                        config.getString("banner").strip(), downstream.healthy(self) ? "up" : "down"));
            case "GET /work":
                return send(resp, 200, Map.of("trabalho", "feito", "downstream", downstream.call(self)));
            case "GET /downstream":
                resp.setContentType("text/plain;charset=utf-8");
                resp.getWriter().write("pronto");
                return 200;
            case "GET /metrics":
                resp.setContentType("application/x-protobuf");
                resp.getOutputStream().write(metrics.snapshot().toByteArray());
                return 200;
            case "GET /debug/scenario":
                Scenario s = scenarios.get(Chaos.checkName(req.getParameter("name")));
                return s == null ? send(resp, 404, Map.of("erro", "cenário desconhecido")) : send(resp, 200, s.describe());
            case "GET /chaos/scenarios":
                return authorized(resp, req) ? send(resp, 200, scenarios.names()) : resp.getStatus();
            case "POST /chaos/scenario":
                if (!authorized(resp, req)) {
                    return resp.getStatus();
                }
                byte[] body = req.getInputStream().readNBytes(MAX_BODY + 1);
                if (body.length > MAX_BODY) {
                    return send(resp, 413, Map.of("erro", "corpo maior que " + MAX_BODY + " bytes"));
                }
                Chaos.Outcome out = chaos.run(scenarioName(body));
                return out == null ? send(resp, 404, Map.of("erro", "cenário desconhecido")) : send(resp, out.status(), out);
            default:
                return send(resp, 404, Map.of("erro", "rota desconhecida"));
        }
    }

    /** Corpo de POST /chaos/scenario: {"name": "..."}; o nome é validado antes de qualquer uso. */
    static String scenarioName(byte[] body) {
        return Chaos.checkName(new JSONObject(new String(body, StandardCharsets.UTF_8)).getString("name"));
    }

    /** /chaos/* exige JWT HS256; sem segredo configurado, o chaos fica desligado (503). */
    private boolean authorized(HttpServletResponse resp, HttpServletRequest req) throws IOException {
        if (jwtSecret == null || jwtSecret.length() < 32) {
            send(resp, 503, Map.of("erro", "chaos desligado: defina CHAOS_JWT_SECRET (32+ caracteres)"));
            return false;
        }
        String auth = req.getHeader("Authorization");
        if (auth == null || !auth.startsWith("Bearer ") || !Jwt.verify(jwtSecret, auth.substring(7))) {
            send(resp, 401, Map.of("erro", "JWT ausente ou inválido"));
            return false;
        }
        return true;
    }

    private static int send(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=utf-8");
        JSON.writeValue(resp.getOutputStream(), body);
        return status;
    }
}
