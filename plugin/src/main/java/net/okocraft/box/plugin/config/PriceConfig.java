package net.okocraft.box.plugin.config;

import com.github.siroshun09.configapi.bukkit.BukkitConfig;
import net.okocraft.box.plugin.Box;
import net.okocraft.box.plugin.model.item.Item;
import org.jetbrains.annotations.NotNull;

public class PriceConfig extends BukkitConfig {

    public PriceConfig(@NotNull Box plugin) {
        super(plugin, "price.yml", true);
    }

    public double getSellingPrice(@NotNull Item item) {
        double value = getDouble(item.getName() + ".sell");

        return 0 < value ? value : 0;
    }

    public double getBuyingPrice(@NotNull Item item) {
        double value = getDouble(item.getName() + ".buy");

        return 0 < value ? value : 0;
    }

    public void setSellingPrice(@NotNull Item item, double price) {
        set(item.getName() + ".sell", 0 < price ? price : 0);
    }

    public void setBuyingPrice(@NotNull Item item, double price) {
        set(item.getName() + ".buy", 0 < price ? price : 0);
    }
}
