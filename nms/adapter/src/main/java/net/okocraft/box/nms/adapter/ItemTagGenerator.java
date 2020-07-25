package net.okocraft.box.nms.adapter;

import net.md_5.bungee.api.chat.ItemTag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemTagGenerator {

    @Nullable
    public static ItemTag generate(@NotNull ItemStack item) {
        switch (ServerVersion.get()) {
            case v1_16_R1:
                return net.okocraft.box.nms.v1_16_R1.ItemTagGenerator.generate(item);
            case v1_15_R1:
                return net.okocraft.box.nms.v1_15_R1.ItemTagGenerator.generate(item);
            default:
                return null;
        }
    }
}
