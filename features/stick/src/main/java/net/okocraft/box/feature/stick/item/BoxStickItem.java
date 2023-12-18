package net.okocraft.box.feature.stick.item;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static net.kyori.adventure.text.Component.empty;
import static net.kyori.adventure.text.Component.translatable;
import static net.kyori.adventure.text.format.NamedTextColor.BLUE;
import static net.kyori.adventure.text.format.NamedTextColor.GRAY;

/**
 * A record to hold a key representing that the item is a Box Stick and provide methods to check if the {@link ItemStack} is Box Item.
 */
public final class BoxStickItem {

    private static final NamespacedKey V3_STICK_KEY = new NamespacedKey("box", "boxstick");

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

    private final @NotNull NamespacedKey key;
    private Consumer<Player> onRightClick;

    /**
     * The constructor of {@link BoxStickItem}.
     *
     * @param key the {@link NamespacedKey} to use for representing that the item is a Box Stick
     */
    @ApiStatus.Internal
    public BoxStickItem(@NotNull NamespacedKey key) {
        this.key = key;
    }

    /**
     * Creates the default stick.
     *
     * @param locale the locale to render the item name and lore.
     * @return the default stick
     */
    public @NotNull ItemStack create(@NotNull Locale locale) {
        var item = new ItemStack(Material.STICK);

        item.editMeta(meta -> makeBoxStick(locale, meta));

        return item;
    }

    private void makeBoxStick(@NotNull Locale locale, @NotNull ItemMeta meta) {
        var displayName = GlobalTranslator.render(DISPLAY_NAME, locale);
        meta.displayName(displayName);

        var lore = LORE.stream().map(c -> GlobalTranslator.render(c, locale)).collect(Collectors.toList());
        meta.lore(lore);

        saveBoxStickKey(meta.getPersistentDataContainer());
    }

    /**
     * Saves the {@link #key()} to {@link PersistentDataContainer}
     *
     * @param target the target {@link PersistentDataContainer}
     */
    public void saveBoxStickKey(@NotNull PersistentDataContainer target) {
        target.set(key, PersistentDataType.BYTE, (byte) 1);
    }

    /**
     * Checks if the {@link ItemStack} is a Box Stick.
     *
     * @param itemStack the {@link ItemStack} to check
     * @return if the {@link ItemStack} has {@link #key()} in {@link PersistentDataContainer}, returns {@code true}, otherwise {@code false}
     */
    public boolean check(@NotNull ItemStack itemStack) {
        var meta = itemStack.getItemMeta();

        if (meta == null) {
            return false;
        }

        return meta.getPersistentDataContainer().has(this.key, PersistentDataType.BYTE) ||
                meta.getPersistentDataContainer().has(V3_STICK_KEY, PersistentDataType.INTEGER);
    }

    public @NotNull NamespacedKey key() {
        return this.key;
    }

    public void onRightClick(@NotNull Consumer<Player> onRightClick) {
        this.onRightClick = onRightClick;
    }

    public void onRightClick(@NotNull Player player) {
        if (this.onRightClick != null) {
            this.onRightClick.accept(player);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BoxStickItem) obj;
        return Objects.equals(this.key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key);
    }

    @Override
    public String toString() {
        return "BoxStickItem[" +
                "key=" + key +
                ']';
    }
}
