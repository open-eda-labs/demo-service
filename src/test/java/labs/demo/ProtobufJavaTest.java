package labs.demo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.protobuf.Struct;
import org.junit.jupiter.api.Test;

/** #8 protobuf-java: snapshot de métricas em Struct, ida e volta em bytes. */
class ProtobufJavaTest {

    @Test
    void snapshotVaiEVoltaEmBytes() throws Exception {
        Metrics metrics = new Metrics(null, null);
        metrics.record("GET /work", 200, 3);
        metrics.record("GET /work", 500, 4);
        Struct back = Struct.parseFrom(metrics.snapshot().toByteArray());
        assertEquals(2.0, back.getFieldsOrThrow("GET /work").getNumberValue());
    }
}
