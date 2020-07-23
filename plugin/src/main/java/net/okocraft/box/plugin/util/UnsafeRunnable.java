package net.okocraft.box.plugin.util;

import java.util.concurrent.CompletionException;

@FunctionalInterface
public interface UnsafeRunnable {

    void run() throws Exception;

    default Runnable toRunnable() {
        return () -> {
            try {
                run();
            } catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException) e;
                } else {
                    throw new CompletionException(e);
                }
            }
        };
    }
}
