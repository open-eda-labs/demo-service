package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import labs.demo.chaos.Chaos;
import org.junit.jupiter.api.Test;

/** #10 jackson-databind (via jackson-bom): contrato do corpo JSON das respostas. */
class JacksonDatabindTest {

    @Test
    void contratoDoResultadoDoChaos() throws Exception {
        assertEquals("{\"scenario\":\"erro-500\",\"status\":500,\"message\":\"falha\",\"runs\":1}",
                Routes.JSON.writeValueAsString(new Chaos.Outcome("erro-500", 500, "falha", 1)));
    }

    @Test
    void contratoDoHealth() throws Exception {
        assertEquals("{\"status\":\"ok\",\"service\":\"s\",\"banner\":\"b\",\"downstream\":\"up\"}",
                Routes.JSON.writeValueAsString(new Routes.Health("ok", "s", "b", "up")));
    }
}
