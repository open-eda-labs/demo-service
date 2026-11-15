package labs.demo.chaos;

import java.util.Random;
import java.util.function.ToIntBiFunction;

/** Devolve o status de erro do cenário. */
final class ErrorFault implements ToIntBiFunction<Scenario, Random> {

    @Override
    public int applyAsInt(Scenario s, Random random) {
        return s.getStatus();
    }
}
