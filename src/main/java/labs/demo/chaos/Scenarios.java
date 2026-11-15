package labs.demo.chaos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Ticker;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.jayway.jsonpath.JsonPath;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;

/** Catálogo de cenários: scenarios.yaml do classpath, em cache por 1 minuto. */
public final class Scenarios {

    static final String FILE = "scenarios.yaml";
    private static final ObjectMapper JSON = new ObjectMapper();
    private final LoadingCache<String, List<Scenario>> cache;

    public Scenarios(Ticker ticker) {
        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(Duration.ofMinutes(1))
                .ticker(ticker)
                .build(CacheLoader.from(Scenarios::load));
    }

    public List<Scenario> all() {
        return cache.getUnchecked(FILE);
    }

    public Scenario get(String name) {
        return all().stream().filter(s -> s.getName().equals(name)).findFirst().orElse(null);
    }

    public List<String> names() throws JsonProcessingException {
        return JsonPath.read(JSON.writeValueAsString(all()), "$[*].name");
    }

    /** Só lê arquivos do classpath do próprio app, nunca entrada HTTP. */
    static List<Scenario> load(String resource) {
        String path = FilenameUtils.normalize(resource, true);
        if (path == null) {
            throw new IllegalArgumentException("caminho fora da raiz: " + resource);
        }
        try (InputStream in = Scenarios.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new IllegalArgumentException("arquivo de cenários não encontrado: " + path);
            }
            Yaml yaml = new Yaml(new Constructor(Scenario.class));
            List<Scenario> scenarios = new ArrayList<>();
            yaml.loadAll(IOUtils.toString(in, StandardCharsets.UTF_8)).forEach(doc -> scenarios.add((Scenario) doc));
            return List.copyOf(scenarios);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
