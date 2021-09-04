package net.okocraft.box.stick.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.box.api.BoxProvider;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.DARK_BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class BoxStickItem {

    private static final Component DISPLAY_NAME = translatable("box.stick.display-name", DARK_BLUE);
    private static final List<Component> LORE = List.of(
            empty(),
            translatable("box.stick.item.lore-1", GRAY),
            translatable("box.stick.item.lore-2", GRAY),
            empty()
    );

    private final NamespacedKey key = BoxProvider.get().createNamespacedKey("stick");

    public ItemStack create(@NotNull Locale locale) {
        var item = new ItemStack(Material.STICK);

        item.editMeta(meta -> makeBoxStick(locale, meta));

        return item;
    }

    private void makeBoxStick(@NotNull Locale locale, @NotNull ItemMeta meta) {
        var displayName = GlobalTranslator.render(DISPLAY_NAME, locale);
        meta.displayName(displayName);

        var lore = LORE.stream().map(c -> GlobalTranslator.render(c, locale)).collect(Collectors.toList());
        meta.lore(lore);

        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
    }

    public boolean check(@NotNull ItemStack itemStack) {
        var meta = itemStack.getItemMeta();

        return meta != null && meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}
