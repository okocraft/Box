package net.okocraft.box.storage.api.util.item;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class ItemNameGenerator {

    public static @NotNull String generate(@NotNull String itemType, byte[] itemBytes) {
        var sha1 = getSha1();
        sha1.reset();
        var result = sha1.digest(itemBytes);
        return itemType + "_" + HexFormat.of().withLowerCase().formatHex(result).substring(0, 8);
    }

    private static @NotNull MessageDigest getSha1() {
        try {
            return MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
