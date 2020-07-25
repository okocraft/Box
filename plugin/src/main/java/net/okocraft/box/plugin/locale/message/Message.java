package net.okocraft.box.plugin.locale.message;

import org.jetbrains.annotations.NotNull;

public enum Message {

    PREFIX("prefix", "&8[&6Box&8]&r "),
    TEST("test", "Test message {} {}"),
    ;

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
        return setPlaceholderNumber();
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
}
