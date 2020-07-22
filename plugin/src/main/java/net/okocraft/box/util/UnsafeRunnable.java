package net.okocraft.box.util;

import java.util.concurrent.CompletionException;

@FunctionalInterface
public interface UnsafeRunnable {

    void run() throws Exception;

    default Runnable toRunnable() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        };
    }
}
