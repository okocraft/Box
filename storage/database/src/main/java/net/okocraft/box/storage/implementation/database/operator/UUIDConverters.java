package net.okocraft.box.storage.implementation.database.operator;

import org.jetbrains.annotations.NotNullByDefault;

import java.nio.ByteBuffer;
import java.util.UUID;

@NotNullByDefault
public final class UUIDConverters {

    public static byte[] toBytes(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return buffer.array();
    }

    public static UUID fromBytes(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return new UUID(buffer.getLong(), buffer.getLong());
    }

    private UUIDConverters() {
        throw new UnsupportedOperationException();
    }
}
