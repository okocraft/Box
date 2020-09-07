package net.okocraft.box.api;

import org.jetbrains.annotations.NotNull;

/**
 * Box への静的アクセスを提供するクラス。
 */
public final class BoxProvider {
    private static Box INSTANCE;

    private BoxProvider() {
        throw new UnsupportedOperationException("This class cannot be instantiated.");
    }

    static void register(@NotNull Box box) {
        INSTANCE = box;
    }

    static void unregister() {
        INSTANCE = null;
    }

    /**
     * Box API を取得する。
     *
     * @return Box API
     * @throws IllegalStateException Box が読み込まれていない場合
     */
    @NotNull
    public static Box get() throws IllegalStateException {
        if (INSTANCE == null) {
            throw new IllegalStateException("The Box API is not loaded.");
        }

        return INSTANCE;
    }
}
