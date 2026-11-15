package labs.demo.chaos;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import org.apache.commons.beanutils.BeanUtils;

/** Um cenário de falha de scenarios.yaml. Bean mutável porque SnakeYAML, XStream e BeanUtils trabalham com setters. */
public class Scenario {

    private String name;
    private String kind;
    private int millis;
    private int status;
    private String message;
    private int bytes;

    /** Propriedades do bean para /debug/scenario. */
    public Map<String, String> describe() {
        try {
            return BeanUtils.describe(this);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public int getMillis() {
        return millis;
    }

    public void setMillis(int millis) {
        this.millis = millis;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getBytes() {
        return bytes;
    }

    public void setBytes(int bytes) {
        this.bytes = bytes;
    }
}
