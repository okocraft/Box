package net.okocraft.box.storage.api.model.item;

public record ItemData(int internalId, String plainName, byte[] itemData) {

    @Override
    public boolean equals(Object o) {
        return o instanceof ItemData other && this.internalId == other.internalId;
    }

    @Override
    public int hashCode() {
        return this.internalId;
    }
}
