package labs.demo;

import com.google.protobuf.Struct;
import com.google.protobuf.Value;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** Contadores por rota: snapshot em protobuf Struct e envio em line protocol para o InfluxDB. */
final class Metrics {

    private static final Logger LOG = LogManager.getLogger(Metrics.class);
    private static final MediaType TEXT = MediaType.get("text/plain; charset=utf-8");
    private final OkHttpClient http = new OkHttpClient.Builder().callTimeout(Duration.ofSeconds(2)).build();
    private final Map<String, LongAdder> counts = new ConcurrentHashMap<>();
    private final String influxUrl;

    /** influxUrl nulo: só conta, não envia. */
    Metrics(String influxUrl) {
        this.influxUrl = influxUrl;
    }

    void record(String route, int status, long millis) {
        counts.computeIfAbsent(route, k -> new LongAdder()).increment();
        if (influxUrl == null) {
            return;
        }
        String line = "demo_requests,route=" + route.replace(" ", "\\ ") + " status=" + status + "i,duration_ms=" + millis + "i";
        Request request = new Request.Builder().url(influxUrl).post(RequestBody.create(line, TEXT)).build();
        try (Response response = http.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                LOG.warn("InfluxDB respondeu {}", response.code());
            }
        } catch (IOException e) {
            LOG.warn("falha ao enviar métricas: {}", e.toString());
        }
    }

    Struct snapshot() {
        Struct.Builder snapshot = Struct.newBuilder();
        counts.forEach((route, n) -> snapshot.putFields(route, Value.newBuilder().setNumberValue(n.sum()).build()));
        return snapshot.build();
    }
}
