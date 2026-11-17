package labs.demo;

import labs.demo.lib.Config;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/** #18 commons-configuration2: app.properties resolve ${url:...} para um arquivo local do app. */
class CommonsConfiguration2Test {

    @Test
    void lookupUrlLeOBannerLocal() {
        assertEquals("demo-service: alvo de laboratório com dependências vulneráveis de propósito",
                Config.load().getString("banner").strip());
    }
}
