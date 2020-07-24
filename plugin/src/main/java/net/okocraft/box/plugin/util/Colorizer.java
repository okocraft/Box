package net.okocraft.box.plugin.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.regex.Pattern;

public class Colorizer {
    private final static char COLOR_MARK = '&';
    private final static char COLOR_CHAR = '§';
    private final static char HEX_MARK = '#';
    private final static char X = 'x';
    private final static String COLOR_CODES = "0123456789AaBbCcDdEeFfKkLlMmNnOoRr";
    private final static Pattern COLOR_PATTERN = Pattern.compile("(?i)" + COLOR_CHAR + "[0-9A-FK-ORX]");

    /**
     * Colorize given string. (convert to minecraft 1.16+ color format)
     * <p>
     * Example: {@code &aHello, &bWorld!} {@literal ->} {@code §aHello, §bWorld!}
     *
     * @param str the string to colorize
     * @return the colorized string
     */
    @NotNull
    public static String colorize(String str) {
        if (str == null) {
            return "";
        }

        char[] b = str.toCharArray();
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < b.length; i++) {
            if (b[i] != COLOR_MARK || b.length == i + 1) {
                builder.append(b[i]);
                continue;
            }

            if (b[i + 1] == HEX_MARK) {
                if (i + 7 < b.length) {
                    try {
                        addMcColor(str.substring(i + 1, i + 8), builder);
                        i += 7;
                        continue;
                    } catch (IllegalArgumentException ignored) {
                    }
                }

                builder.append(b[i]).append(b[i + 1]);
                i++;
                continue;
            }

            if (-1 < COLOR_CODES.indexOf(b[i + 1])) {
                builder.append(COLOR_CHAR).append(Character.toLowerCase(b[i + 1]));
                i++;
            } else {
                builder.append(b[i]);
            }
        }

        return builder.toString();
    }

    /**
     * Strips the given string of color.
     * <p>
     * Example: {@code §aHello, §bWorld!} {@literal ->} {@code Hello, World!}
     *
     * @param str the string to strip
     * @return stripped string
     */
    @NotNull
    public static String stripColor(String str) {
        if (str == null) {
            return "";
        }

        return COLOR_PATTERN.matcher(str).replaceAll("");
    }

    /**
     * Convert given hex to minecraft color format.
     * <p>
     * Example: {@code #123456} {@literal ->} {@code §x§1§2§3§4§5§6}
     *
     * @param hex the hex
     * @return minecraft color
     * @throws IllegalArgumentException when hex cannot be converted.
     */
    @NotNull
    public static String toMcColor(@NotNull String hex) throws IllegalArgumentException {
        Objects.requireNonNull(hex, "hex");

        return addMcColor(hex, new StringBuilder()).toString();
    }

    @Contract("_, _ -> param2")
    @NotNull
    private static StringBuilder addMcColor(@NotNull String hex,
                                            @NotNull StringBuilder builder) throws IllegalArgumentException {
        char[] array = hex.toCharArray();

        if (array.length != 7) {
            throw new IllegalArgumentException("hex must be 7 characters.");
        }

        if (array[0] != HEX_MARK) {
            throw new IllegalArgumentException("hex must start with #");
        }

        try {
            Integer.parseInt(hex.substring(1), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Could not parse hex: " + hex, e);
        }

        builder.append(COLOR_CHAR).append(X);

        for (char c : hex.substring(1).toCharArray()) {
            builder.append(COLOR_CHAR).append(c);
        }

        return builder;
    }
}
