package net.okocraft.box.plugin.util;

import net.okocraft.box.api.Box;
import net.okocraft.box.api.BoxProvider;

import java.lang.reflect.Method;

/**
 * {@link BoxProvider} のメソッドを実行し、 {@link Box} を API として提供できるようにするクラス。
 */
public final class APIRegisterer {

    private static final Method REGISTER;
    private static final Method UNREGISTER;

    static {
        try {
            REGISTER = BoxProvider.class.getDeclaredMethod("register", Box.class);
            REGISTER.setAccessible(true);

            UNREGISTER = BoxProvider.class.getDeclaredMethod("unregister");
            UNREGISTER.setAccessible(true);
        } catch (Throwable e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    /**
     * Box を API として登録する。
     *
     * @param boxApi 登録する Box インスタンス
     */
    public static void register(Box boxApi) {
        try {
            REGISTER.invoke(null, boxApi);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    /**
     * Box API を登録解除する。
     */
    public static void unregister() {
        try {
            UNREGISTER.invoke(null);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
