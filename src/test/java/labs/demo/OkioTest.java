package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** #12 okio (via okhttp): métricas em line protocol enviadas a um stub do InfluxDB. */
class OkioTest {

    @Test
    void enviaLineProtocol() throws Exception {
        try (StubServer influx = new StubServer("/api/v2/write", "")) {
            new Metrics(influx.base() + "/api/v2/write?bucket=demo", "token-de-teste").record("GET /work", 200, 7);
            assertEquals("demo_requests,route=GET\\ /work status=200i,duration_ms=7i", influx.received.poll(5, TimeUnit.SECONDS));
            assertEquals("Token token-de-teste", influx.authorization);
        }
    }
}
