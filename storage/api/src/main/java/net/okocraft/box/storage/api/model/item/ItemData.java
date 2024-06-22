package net.okocraft.box.storage.api.model.item;

import org.jetbrains.annotations.NotNull;

public record ItemData(int internalId, String plainName, byte[] itemData) implements NamedItem<byte[]> {

    @Override
    public byte @NotNull [] item() {
        return this.itemData;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemData other && this.internalId == other.internalId;
    }

    @Override
    public int hashCode() {
        return this.internalId;
    }
}
