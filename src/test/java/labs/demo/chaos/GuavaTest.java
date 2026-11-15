package labs.demo.chaos;

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.google.common.base.Ticker;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;

/** #3 guava: CacheBuilder com Ticker falso guarda os cenários por 1 minuto. */
class GuavaTest {

    @Test
    void cacheExpiraDepoisDeUmMinuto() {
        AtomicLong nanos = new AtomicLong();
        Scenarios scenarios = new Scenarios(new Ticker() {
            @Override
            public long read() {
                return nanos.get();
            }
        });
        Object first = scenarios.all();
        nanos.addAndGet(TimeUnit.SECONDS.toNanos(59));
        assertSame(first, scenarios.all());
        nanos.addAndGet(TimeUnit.SECONDS.toNanos(2));
        assertNotSame(first, scenarios.all());
    }
}
