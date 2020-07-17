package net.okocraft.box.api;

import net.okocraft.box.api.item.ItemManager;
import net.okocraft.box.api.user.UserManager;
import org.jetbrains.annotations.NotNull;

/**
 * Box API クラス。
 */
public interface Box {

    @NotNull UserManager getUserManager();

    @NotNull ItemManager getItemManager();
}
