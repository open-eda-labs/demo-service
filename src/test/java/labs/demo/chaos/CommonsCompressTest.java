package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** #20 commons-compress: o cenário OOM compacta e descompacta um payload em memória. */
class CommonsCompressTest {

    @Test
    void idaEVolta() throws Exception {
        assertEquals(1_000_000, OomFault.unzip(OomFault.zip(1_000_000), OomFault.LIMIT));
    }

    @Test
    void acimaDoLimiteParaAntesDeAlocar() throws Exception {
        assertEquals(-1, OomFault.unzip(OomFault.zip(OomFault.LIMIT + 1), OomFault.LIMIT));
    }
}
