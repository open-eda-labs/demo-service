package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** #14 h2: histórico de falhas em memória (colunas key/value), insert e consulta. */
class H2Test {

    @Test
    void gravaEConta() {
        History history = new History();
        assertEquals(1, history.record("erro-500", "500"));
        assertEquals(2, history.record("erro-500", "500"));
        assertEquals(1, history.record("oom-1mb", "200"));
    }
}
