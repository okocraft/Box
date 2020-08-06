package net.okocraft.box.plugin.util;

import java.util.concurrent.CompletionException;

/**
 * 検査例外を投げることのできる Runnable。
 * <p>
 * {@link UnsafeRunnable#toRunnable()} で通常の {@link Runnable} にでき、
 * その中で例外が発生した場合は {@link CompletionException} (または {@link RuntimeException}) が投げられる。
 * <p>
 * これは関数型インターフェースのため、ラムダ式またはメソッド参照の代入先として使用できる。
 */
@FunctionalInterface
public interface UnsafeRunnable {

    /**
     * 検査例外を投げることのできる run メソッド。
     *
     * @throws Exception 実行中に投げられる例外
     */
    void run() throws Exception;

    /**
     * {@link UnsafeRunnable#run()} を try-catch でラップし、通常の {@link Runnable} にして返す。
     * <p>
     * 実行中に例外が発生した場合は {@link CompletionException} (または {@link RuntimeException}) をスルーする。
     *
     * @return {@link UnsafeRunnable#run()} を try-catch で実行する {@link Runnable}
     */
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
