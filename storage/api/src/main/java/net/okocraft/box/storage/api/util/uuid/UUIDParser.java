package net.okocraft.box.storage.api.util.uuid;

import net.okocraft.box.storage.api.holder.LoggerHolder;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class UUIDParser {

    public static @Nullable UUID parseOrWarn(@Nullable String strUuid) {
        if (strUuid != null) {
            try {
                return UUID.fromString(strUuid);
            } catch (IllegalArgumentException e) {
                LoggerHolder.get().warning("Invalid uuid: " + strUuid);
            }
        }

        return null;
    }
}
