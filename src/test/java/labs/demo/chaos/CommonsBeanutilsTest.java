package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import org.junit.jupiter.api.Test;

/** #16 commons-beanutils: BeanUtils.describe alimenta /debug/scenario. */
class CommonsBeanutilsTest {

    @Test
    void conjuntoDeChaves() {
        assertEquals(Set.of("class", "name", "kind", "millis", "status", "message", "bytes"),
                Scenarios.load(Scenarios.FILE).get(1).describe().keySet());
    }
}
