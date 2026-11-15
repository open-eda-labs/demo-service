package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

/** #15 xstream: snapshot do cenário em XML, ida e volta. */
class XStreamTest {

    @Test
    void copiaPorXml() {
        Scenario original = Scenarios.load(Scenarios.FILE).get(3);
        Scenario copy = Chaos.copy(original);
        assertNotSame(original, copy);
        assertEquals(original.describe(), copy.describe());
    }
}
