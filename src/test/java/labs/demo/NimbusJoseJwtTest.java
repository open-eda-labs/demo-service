package labs.demo;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.SecureRandom;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/** #6 nimbus-jose-jwt: JWT HS256 que protege /chaos/*. */
class NimbusJoseJwtTest {

    static final String SECRET = secret();

    static String secret() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }

    @Test
    void tokenValidoPassa() throws Exception {
        assertTrue(Jwt.verify(SECRET, Jwt.sign(SECRET, 60)));
    }

    @Test
    void tokenExpiradoFalha() throws Exception {
        assertFalse(Jwt.verify(SECRET, Jwt.sign(SECRET, -60)));
    }

    @Test
    void tokenDeOutroSegredoOuLixoFalha() throws Exception {
        assertFalse(Jwt.verify(SECRET, Jwt.sign(secret(), 60)));
        assertFalse(Jwt.verify(SECRET, "nao-e-um-jwt"));
    }
}
