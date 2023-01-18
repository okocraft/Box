package net.okocraft.box.storage.api.util.item;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.model.item.BoxItem;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.IntFunction;

public final class BoxItemSupplier {

    public static IntFunction<BoxItem> ITEM_FUNCTION;

    public static @NotNull Optional<BoxItem> getItem(int internalId) {
        if (ITEM_FUNCTION == null) {
            return BoxProvider.get().getItemManager().getBoxItem(internalId);
        }

        return Optional.ofNullable(ITEM_FUNCTION.apply(internalId));
    }
}
