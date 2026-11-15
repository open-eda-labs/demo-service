package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

/** #13 snakeyaml: new Yaml(new Constructor(Scenario.class)) carrega scenarios.yaml. */
class SnakeYamlTest {

    @Test
    void carregaOsCenarios() {
        List<Scenario> all = Scenarios.load(Scenarios.FILE);
        assertEquals(5, all.size());
        Scenario latency = all.get(0);
        assertEquals("latencia-200ms", latency.getName());
        assertEquals("LatencyFault", latency.getKind());
        assertEquals(200, latency.getMillis());
        assertEquals(33554432, all.get(4).getBytes());
    }
}
