package net.okocraft.box.plugin.locale.message;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public enum Message {

    // プレースホルダーは {} で設定する。デフォルトのメッセージは、左から 0, 1, ... とナンバリングされる。
    PREFIX("prefix", "&8[&6Box&8]&r "),
    TEST("test", "Test message {} {}"),
    ;

    private final static Cache<Message, String> DEFAULT_MESSAGE =
            CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.MINUTES).build();

    private final String path;
    private final String def;

    Message(@NotNull String path, @NotNull String def) {
        this.path = path;
        this.def = def;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    @NotNull
    public String getDefault() {
        try {
            return DEFAULT_MESSAGE.get(this, this::setPlaceholderNumber);
        } catch (ExecutionException e) {
            e.printStackTrace();
            return setPlaceholderNumber();
        }
    }

    @NotNull
    private String setPlaceholderNumber() {
        StringBuilder builder = new StringBuilder();
        char[] b = def.toCharArray();
        int count = 0;

        for (int i = 0; i < b.length; i++) {
            if (b[i] == '{' && i + 1 < b.length && b[i + 1] == '}') {
                builder.append('{').append(count).append('}');
                count++;
                i++;
            } else {
                builder.append(b[i]);
            }
        }

        return builder.toString();
    }

    public static void clearCache() {
        DEFAULT_MESSAGE.invalidateAll();
    }
}
