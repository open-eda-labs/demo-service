package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** #9 commons-lang3: Validate/StringUtils nos parâmetros e ClassUtils.getClass para a estratégia de falha. */
class CommonsLang3Test {

    @Test
    void resolveAEstrategiaPeloNome() {
        assertInstanceOf(LatencyFault.class, Chaos.fault("LatencyFault"));
        assertInstanceOf(OomFault.class, Chaos.fault("OomFault"));
        assertThrows(IllegalArgumentException.class, () -> Chaos.fault("java.lang.Runtime"));
        assertThrows(IllegalArgumentException.class, () -> Chaos.fault("NadaFault"));
    }

    @Test
    void validaONomeDoCenario() {
        assertEquals("erro-500", Chaos.checkName("  erro-500 "));
        assertThrows(IllegalArgumentException.class, () -> Chaos.checkName(null));
        assertThrows(IllegalArgumentException.class, () -> Chaos.checkName("../etc"));
        assertThrows(IllegalArgumentException.class, () -> Chaos.checkName("a".repeat(41)));
    }
}
