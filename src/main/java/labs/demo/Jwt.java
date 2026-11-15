package labs.demo;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Date;
import java.util.Objects;

/** JWT HS256 que protege /chaos/*. O main imprime um token de 1 hora para o chaos/run-scenarios.sh. */
public final class Jwt {

    public static void main(String[] args) throws JOSEException {
        System.out.println(sign(Objects.requireNonNull(System.getenv("CHAOS_JWT_SECRET"), "defina CHAOS_JWT_SECRET"), 3600));
    }

    public static String sign(String secret, long ttlSeconds) throws JOSEException {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .subject("chaos")
                .expirationTime(new Date(System.currentTimeMillis() + ttlSeconds * 1000))
                .build();
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims);
        jwt.sign(new MACSigner(secret.getBytes(StandardCharsets.UTF_8)));
        return jwt.serialize();
    }

    public static boolean verify(String secret, String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            Date exp = jwt.getJWTClaimsSet().getExpirationTime();
            return JWSAlgorithm.HS256.equals(jwt.getHeader().getAlgorithm())
                    && jwt.verify(new MACVerifier(secret.getBytes(StandardCharsets.UTF_8)))
                    && exp != null && exp.after(new Date());
        } catch (ParseException | JOSEException e) {
            return false;
        }
    }
}
