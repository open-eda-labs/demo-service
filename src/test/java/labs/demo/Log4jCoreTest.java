package labs.demo;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringWriter;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.WriterAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.eclipse.jetty.server.Server;
import org.junit.jupiter.api.Test;

/** #1 log4j-core (e log4j-api): cada requisição gera uma linha de log. */
class Log4jCoreTest {

    @Test
    void logaCadaRequisicaoNumAppenderEmMemoria() throws Exception {
        StringWriter out = new StringWriter();
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Appender memory = WriterAppender.createAppender(PatternLayout.newBuilder().withPattern("%p %m%n").build(),
                null, out, "memoria", false, true);
        memory.start();
        ctx.getRootLogger().addAppender(memory);
        Server server = App.start("127.0.0.1", 0, Map.of());
        try {
            HttpClient.newHttpClient().send(HttpRequest.newBuilder(
                    URI.create("http://127.0.0.1:" + App.port(server) + "/downstream")).build(), HttpResponse.BodyHandlers.discarding());
        } finally {
            server.stop();
            ctx.getRootLogger().removeAppender(memory);
        }
        assertTrue(out.toString().contains("INFO requisicao rota=GET /downstream status=200"), out.toString());
    }
}
