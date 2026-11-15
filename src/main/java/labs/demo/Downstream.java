package labs.demo;

import java.io.IOException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/** Chamadas ao downstream simulado (GET /downstream do próprio app). */
final class Downstream {

    private final CloseableHttpClient http = HttpClients.custom()
            .setDefaultRequestConfig(RequestConfig.custom().setConnectTimeout(2000).setSocketTimeout(5000).build())
            .build();
    // RestTemplate sobre o HttpClient 4: no Spring 6 este construtor passa a exigir o HttpClient 5.
    private final RestTemplate rest = new RestTemplate(new HttpComponentsClientHttpRequestFactory(http));

    boolean healthy(String base) {
        try (CloseableHttpResponse response = http.execute(new HttpGet(base + "/downstream"))) {
            return response.getStatusLine().getStatusCode() == 200;
        } catch (IOException e) {
            return false;
        }
    }

    String call(String base) {
        return rest.getForObject(base + "/downstream", String.class);
    }
}
