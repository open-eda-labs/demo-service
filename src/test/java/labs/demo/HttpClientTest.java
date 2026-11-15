package labs.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** #5 httpclient: health-check do downstream simulado. */
class HttpClientTest {

    @Test
    void downstreamNoArEstaSaudavel() throws Exception {
        try (StubServer stub = new StubServer("/downstream", "pronto")) {
            assertTrue(new Downstream().healthy(stub.base()));
        }
    }

    @Test
    void downstreamForaDoArNaoEstaSaudavel() throws Exception {
        String base;
        try (StubServer stub = new StubServer("/downstream", "pronto")) {
            base = stub.base();
        }
        assertFalse(new Downstream().healthy(base));
    }
}
