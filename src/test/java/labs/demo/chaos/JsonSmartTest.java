package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.common.base.Ticker;
import java.util.List;
import org.junit.jupiter.api.Test;

/** #11 json-smart (via json-path): JsonPath.read extrai os nomes dos cenários. */
class JsonSmartTest {

    @Test
    void extraiNomesComJsonPath() throws Exception {
        assertEquals(List.of("latencia-200ms", "erro-500", "erro-503", "oom-1mb", "oom-acima-do-limite"),
                new Scenarios(Ticker.systemTicker()).names());
    }
}
