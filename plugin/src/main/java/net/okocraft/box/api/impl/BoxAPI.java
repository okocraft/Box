package net.okocraft.box.api.impl;

import net.okocraft.box.api.Box;
import net.okocraft.box.api.impl.item.ApiItemManager;
import net.okocraft.box.api.impl.user.ApiUserManager;
import net.okocraft.box.api.item.ItemManager;
import net.okocraft.box.api.user.UserManager;
import org.jetbrains.annotations.NotNull;

public final class BoxAPI implements Box {

    private final UserManager userManager = new ApiUserManager();
    private final ItemManager itemManager = new ApiItemManager();

    @Override
    public @NotNull UserManager getUserManager() {
        return userManager;
    }

    @Override
    public @NotNull ItemManager getItemManager() {
        return itemManager;
    }
}
