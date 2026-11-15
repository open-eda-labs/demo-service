package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/** #4 commons-io: FilenameUtils.normalize e IOUtils ao ler o arquivo de cenários. */
class CommonsIoTest {

    @Test
    void caminhosRelativosDentroDaRaiz() {
        int expected = Scenarios.load(Scenarios.FILE).size();
        assertEquals(expected, Scenarios.load("./scenarios.yaml").size());
        assertEquals(expected, Scenarios.load("sub/../scenarios.yaml").size());
    }

    @Test
    void caminhoForaDaRaizERecusado() {
        assertThrows(IllegalArgumentException.class, () -> Scenarios.load("../scenarios.yaml"));
        assertThrows(IllegalArgumentException.class, () -> Scenarios.load("nao-existe.yaml"));
    }
}
