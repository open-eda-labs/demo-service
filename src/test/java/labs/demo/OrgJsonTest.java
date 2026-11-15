package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;
import org.json.JSONException;
import org.junit.jupiter.api.Test;

/** #7 org.json (profile chaos): parse do corpo de POST /chaos/scenario. */
class OrgJsonTest {

    @Test
    void extraiONomeDoCenario() {
        assertEquals("erro-500", Routes.scenarioName("{\"name\": \" erro-500 \"}".getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    void corpoInvalidoFalha() {
        assertThrows(JSONException.class, () -> Routes.scenarioName("{".getBytes(StandardCharsets.UTF_8)));
        assertThrows(IllegalArgumentException.class, () -> Routes.scenarioName("{\"name\": \"../x\"}".getBytes(StandardCharsets.UTF_8)));
    }
}
