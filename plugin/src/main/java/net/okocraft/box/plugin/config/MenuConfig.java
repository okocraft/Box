package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.gui.button.ButtonIcon;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MenuConfig extends BukkitConfig {

    private final Box plugin;

    public MenuConfig(@NotNull Box plugin) {
        super(plugin, "menu.yml", true);

        this.plugin = plugin;
    }

    public void applyConfig(ButtonIcon icon, String configPath) {        
        Material type = Material.matchMaterial(getString(configPath + ".material"));
        if (type != null) {
            icon.setType(type);
        }
        
        String displayName = getConfig().getString(configPath + ".display-name", null);
        icon.setDisplayName(displayName);
        
        List<String> lore = getConfig().getStringList(configPath + ".lore");
        if (getConfig().isList(configPath + ".lore") && !lore.isEmpty()) {
            icon.setLore(lore);
        }
        
        if (getConfig().isBoolean(configPath + ".glow")) {
            icon.setGlowing(getBoolean(configPath + ".glow", false));
        }
    }
}
