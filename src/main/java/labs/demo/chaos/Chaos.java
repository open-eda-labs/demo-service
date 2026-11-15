package labs.demo.chaos;

import com.thoughtworks.xstream.XStream;
import java.util.Map;
import java.util.Random;
import java.util.function.ToIntBiFunction;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.text.StringSubstitutor;

/** Executa um cenário: resolve a falha pelo nome, injeta, registra no histórico. */
public final class Chaos {

    // Sem allowTypes de propósito (caso #15). Só lê o XML que acabou de gerar, nunca entrada HTTP.
    private static final XStream XSTREAM = new XStream();

    public record Outcome(String scenario, int status, String message, long runs) {}

    private final Scenarios scenarios;
    private final History history;
    private final Random random;

    /** seed fixa: a mesma seed repete a mesma sequência de jitter. */
    public Chaos(Scenarios scenarios, History history, long seed) {
        this.scenarios = scenarios;
        this.history = history;
        this.random = new Random(seed);
    }

    /** null se o cenário não existe. */
    public Outcome run(String name) {
        Scenario found = scenarios.get(name);
        if (found == null) {
            return null;
        }
        Scenario s = copy(found);
        int status = fault(s.getKind()).applyAsInt(s, random);
        long runs = history.record(s.getName(), String.valueOf(status));
        return new Outcome(s.getName(), status, message(s, status), runs);
    }

    public static String checkName(String name) {
        String trimmed = StringUtils.trimToEmpty(name);
        Validate.matchesPattern(trimmed, "[a-z0-9-]{1,40}", "nome de cenário inválido");
        return trimmed;
    }

    /** Resolve a classe de falha deste pacote pelo campo kind (ex.: LatencyFault). */
    @SuppressWarnings("unchecked")
    static ToIntBiFunction<Scenario, Random> fault(String kind) {
        Validate.isTrue(StringUtils.isAlpha(kind) && kind.endsWith("Fault"), "tipo de falha inválido: %s", kind);
        try {
            return (ToIntBiFunction<Scenario, Random>) ClassUtils.getClass(Chaos.class.getPackageName() + "." + kind)
                    .getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("tipo de falha desconhecido: " + kind, e);
        }
    }

    /** Só StringSubstitutor com Map: nenhum lookup (${env:}, ${script:} etc.) é resolvido. */
    static String message(Scenario s, int status) {
        return new StringSubstitutor(Map.of("name", s.getName(), "status", String.valueOf(status),
                "millis", String.valueOf(s.getMillis()), "bytes", String.valueOf(s.getBytes()))).replace(s.getMessage());
    }

    /** Snapshot em XML e de volta: a falha recebe uma cópia, nunca o cenário em cache. */
    static Scenario copy(Scenario s) {
        return (Scenario) XSTREAM.fromXML(XSTREAM.toXML(s));
    }
}
