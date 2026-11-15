package labs.demo;

import java.io.IOException;
import java.io.InputStream;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileHandler;

/** Lê app.properties do classpath. */
final class Config {

    static PropertiesConfiguration load() {
        PropertiesConfiguration config = new PropertiesConfiguration();
        try (InputStream in = Config.class.getResourceAsStream("/app.properties")) {
            new FileHandler(config).load(in);
        } catch (IOException | ConfigurationException e) {
            throw new IllegalStateException("app.properties ilegível", e);
        }
        return config;
    }
}
