package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.gui.item.MenuIcon;
import net.okocraft.box.plugin.util.ItemBuilder;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuConfig extends BukkitConfig {

    private final Box plugin;

    public MenuConfig(@NotNull Box plugin) {
        super(plugin, "menu.yml", true);

        this.plugin = plugin;
    }

    public ItemBuilder getItemBuilder(@NotNull MenuIcon icon) {
        return new ItemBuilder()
                .setMaterial(getMaterial(icon))
                .setDisplayName(getDisplayName(icon))
                .setLore(getLore(icon))
                .setGlowing(isGlowing(icon));
    }

    @NotNull
    private Material getMaterial(@NotNull MenuIcon icon) {
        String path = icon.getRootPath() + ".material";
        String value = getString(path);

        try {
            return Material.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name: " + value + " (" + path + ")");
            return icon.getDefaultMaterial();
        }
    }

    @Nullable
    private String getDisplayName(@NotNull MenuIcon icon) {
        // null (デフォルトのアイテム名) を許容するため YamlConfiguration から取得する
        return getConfig().getString(icon.getRootPath() + ".display-name", icon.getDefaultName());
    }

    @NotNull
    private List<String> getLore(@NotNull MenuIcon icon) {
        return getStringList(icon.getRootPath() + ".lore", icon.getDefaultLore());
    }

    private boolean isGlowing(@NotNull MenuIcon icon) {
        return getBoolean(icon.getRootPath() + ".glow", icon.isGlowing());
    }
}
