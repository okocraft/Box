package net.okocraft.box.api.util;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.Keyed;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

/**
 * A utility class for generating item names.
 */
@ApiStatus.Experimental
public final class ItemNameGenerator {

    /**
     * Creates the name from the {@link Key}.
     * <p>
     * If {@link Key#namespace()} is {@link Key#MINECRAFT_NAMESPACE}, this method returns only uppercase {@link Key#value()}.
     * Otherwise, returns the name in the format {@code NAMESPACE_VALUE}.
     *
     * @param key the {@link Key} to create the name
     * @return the created name
     */
    public static @NotNull String key(@NotNull Key key) {
        if (key.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
            return key.value().toUpperCase(Locale.ENGLISH);
        } else {
            return key.namespace().toUpperCase(Locale.ENGLISH) + "_" + key.value().toUpperCase(Locale.ENGLISH);
        }
    }

    /**
     * Creates the name from the {@link Keyed}.
     *
     * @param key the {@link Keyed} to create the name
     * @return the created name
     * @see #key(Key)
     */
    public static @NotNull String key(@NotNull Keyed key) {
        return key(key.key());
    }

    /**
     * Creates the name from the {@link Keyed}s.
     * <p>
     * This method returns the name of two {@link #key(Keyed)} results joined by {@code _}.
     *
     * @param key1 the first {@link Keyed} to create the name
     * @param key2 the second {@link Keyed} to create the name
     * @return the created name
     * @see #key(Key)
     */
    public static @NotNull String keys(@NotNull Keyed key1, @NotNull Keyed key2) {
        return key(key1) + "_" + key(key2);
    }

    /**
     * Creates the name from the {@link Keyed}s.
     * <p>
     * This method returns the name of {@link #key(Keyed)} results joined by {@code _}.
     *
     * @param key1 the first {@link Keyed} to create the name
     * @param key2 the second {@link Keyed} to create the name
     * @param keys the other {@link Keyed}s to create the name
     * @return the created name
     * @see #key(Key)
     */
    public static @NotNull String keys(@NotNull Keyed key1, @NotNull Keyed key2, @NotNull Keyed @NotNull ... keys) {
        if (keys.length == 0) {
            return keys(key1, key2);
        }

        StringBuilder builder = new StringBuilder().append(key(key1)).append('_').append(key(key2));

        for (Keyed key : keys) {
            builder.append('_').append(key(key));
        }

        return builder.toString();
    }

    /**
     * Creates the name from the {@link ItemStack}.
     * <p>
     * This method passes {@link ItemStack#getType()} and {@link ItemStack#serializeAsBytes()} to {@link #itemStack(Keyed, byte[])}.
     *
     * @param itemStack the {@link ItemStack} to create the name
     * @return the created name
     * @see #itemStack(Keyed, byte[])
     */
    public static @NotNull String itemStack(@NotNull ItemStack itemStack) {
        return itemStack(itemStack.getType(), itemStack.serializeAsBytes());
    }

    /**
     * Creates the name from the {@link ItemStack}.
     * <p>
     * This method returns the name in the format {@code key_sha1}.
     *
     * @param key   the {@link Keyed} to create the name
     * @param bytes the bytes to create the hash value by SHA-1
     * @return the created name
     */
    public static @NotNull String itemStack(@NotNull Keyed key, byte @NotNull [] bytes) {
        Objects.requireNonNull(bytes);
        return key(key) + "_" + HexFormat.of().withLowerCase().formatHex(sha1().digest(bytes)).substring(0, 8);
    }

    private static @NotNull MessageDigest sha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private ItemNameGenerator() {
        throw new UnsupportedOperationException();
    }
}
