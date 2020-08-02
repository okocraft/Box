package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.util.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MenuConfig extends BukkitConfig {

    private final Box plugin;

    public MenuConfig(@NotNull Box plugin) {
        super(plugin, "menu.yml", true);

        this.plugin = plugin;
    }

    @NotNull
    public ItemStack getFlame() {
        return getItemBuilder("flame", "&7", Material.GRAY_STAINED_GLASS_PANE).build();
    }

    @NotNull
    private ItemBuilder getItemBuilder(@NotNull String root, @NotNull String defName, @NotNull Material defMaterial) {
        return new ItemBuilder()
                .setDisplayName(getDisplayName(root, defName))
                .setLore(getLore(root))
                .setGlowing(isGlowing(root))
                .setMaterial(getMaterial(root, defMaterial));
    }

    @NotNull
    private Material getMaterial(@NotNull String root, @NotNull Material def) {
        String path = root + ".material";
        String value = getString(path);

        try {
            return Material.valueOf(value);
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid material name: " + value + " (" + path + ")");
            return def;
        }
    }

    @Nullable
    private String getDisplayName(@NotNull String root, @Nullable String def) {
        // null (デフォルトのアイテム名) を許容するため YamlConfiguration から取得する
        return getConfig().getString(root + ".display-name", def);
    }

    @NotNull
    private List<String> getLore(@NotNull String root) {
        return getStringList(root + ".lore");
    }

    private boolean isGlowing(@NotNull String root) {
        return getBoolean(root + ".glow");
    }
}
