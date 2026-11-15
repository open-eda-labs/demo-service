package labs.demo.chaos;

import java.util.Random;
import java.util.function.ToIntBiFunction;

/** Dorme millis mais um jitter de até 25% sorteado com a seed do Chaos. */
final class LatencyFault implements ToIntBiFunction<Scenario, Random> {

    @Override
    public int applyAsInt(Scenario s, Random random) {
        try {
            Thread.sleep(s.getMillis() + random.nextInt(s.getMillis() / 4 + 1));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return s.getStatus();
    }
}
