package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** #2 commons-text: StringSubstitutor com Map monta a mensagem da falha injetada. */
class CommonsTextTest {

    static Scenario scenario(String message) {
        Scenario s = new Scenario();
        s.setName("erro-500");
        s.setMessage(message);
        return s;
    }

    @Test
    void montaAMensagem() {
        assertEquals("falha injetada no cenário erro-500 com status 500",
                Chaos.message(scenario("falha injetada no cenário ${name} com status ${status}"), 500));
    }

    @Test
    void naoResolveLookups() {
        assertEquals("x ${env:HOME} ${script:js:1}", Chaos.message(scenario("x ${env:HOME} ${script:js:1}"), 500));
    }
}
