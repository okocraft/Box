package net.okocraft.box.plugin.locale.formatter;

import org.jetbrains.annotations.NotNull;

/**
 * プレースホルダーを置換するクラス。
 *
 * 可変長変数での配列生成コストを抑えるため、3つまでは単体メソッドで対応する。
 */
public final class Formatter {

    @NotNull
    public static String format(@NotNull String message, @NotNull String holder1) {
        return message.replace("{0}", holder1);
    }

    @NotNull
    public static String format(@NotNull String message, @NotNull String holder1, @NotNull String holder2) {
        return message.replace("{0}", holder1).replace("{1}", holder2);
    }

    @NotNull
    public static String format(@NotNull String message,
                                @NotNull String holder1, @NotNull String holder2, @NotNull String holder3) {
        return message.replace("{0}", holder1).replace("{1}", holder2).replace("{2}", holder3);
    }

    @NotNull
    public static String format(@NotNull String message, @NotNull String... holders) {
        String msg = message;
        int count = 0;

        for (String holder : holders) {
            msg = msg.replace("{" + count + "}", holder);
            count++;
        }

        return msg;
    }
}
