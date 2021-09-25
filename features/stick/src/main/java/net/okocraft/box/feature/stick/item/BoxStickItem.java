package net.okocraft.box.feature.stick.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

public class BoxStickItem {

    private static final Style NO_DECORATION =
            Style.style().decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();

    private static final Component DISPLAY_NAME =
            translatable("box.stick.item.display-name", NO_DECORATION.color(BLUE));

    private static final List<Component> LORE = List.of(
            empty(),
            translatable("box.stick.item.lore-1", NO_DECORATION.color(GRAY)),
            translatable("box.stick.item.lore-2", NO_DECORATION.color(GRAY)),
            empty(),
            translatable("box.stick.item.lore-3", NO_DECORATION.color(GRAY)),
            empty()
    );

    private final NamespacedKey key;

    public BoxStickItem(@NotNull NamespacedKey key) {
        this.key = key;
    }

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
