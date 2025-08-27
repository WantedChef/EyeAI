package chef.sheesh.eyeAI.infra.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class Async {
    public static final ExecutorService IO = Executors.newFixedThreadPool(4);
}
