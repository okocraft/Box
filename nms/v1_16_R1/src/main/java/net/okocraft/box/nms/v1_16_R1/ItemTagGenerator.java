package net.okocraft.box.nms.v1_16_R1;

import net.md_5.bungee.api.chat.ItemTag;
import net.minecraft.server.v1_16_R1.NBTTagCompound;
import org.bukkit.craftbukkit.v1_16_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ItemTagGenerator {

    @Nullable
    public static ItemTag generate(@NotNull ItemStack item) {
        NBTTagCompound nbt = CraftItemStack.asNMSCopy(item).getTag();
        return nbt != null ? ItemTag.ofNbt(nbt.toString()) : null;
    }
}
