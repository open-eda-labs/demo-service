package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** #17 spring-web: RestTemplate sobre HttpClient 4 chama o downstream. */
class SpringWebTest {

    @Test
    void restTemplateChamaODownstream() throws Exception {
        try (StubServer stub = new StubServer("/downstream", "pronto")) {
            assertEquals("pronto", new Downstream().call(stub.base()));
        }
    }
}
