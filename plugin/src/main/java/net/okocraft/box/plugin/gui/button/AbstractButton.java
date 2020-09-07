package net.okocraft.box.plugin.gui.button;

import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import net.okocraft.box.plugin.Box;

import java.util.Objects;

public abstract class AbstractButton implements Button {

    //FIXME: Boxのプラグインインスタンスの取得方法が行方不明
    protected static final Box PLUGIN = JavaPlugin.getPlugin(Box.class);

    protected ButtonIcon icon;

    public AbstractButton(@NotNull ButtonIcon icon) {
        this.icon = icon;
    }

    @NotNull
    @Override
    public ButtonIcon getIcon() {
        return icon;
    }

    @Override
    public void setIcon(ButtonIcon icon) {
        this.icon = icon;
    }

    protected static ButtonIcon createHeadIcon(OfflinePlayer player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        meta.setOwningPlayer(player);
        head.setItemMeta(meta);
        return new ButtonIcon(head);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof AbstractButton)) {
            return false;
        }

        AbstractButton that = (AbstractButton) o;
        return icon.equals(that.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(icon);
    }
}
