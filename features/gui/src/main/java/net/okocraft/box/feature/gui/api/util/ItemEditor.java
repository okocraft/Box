package net.okocraft.box.feature.gui.api.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ItemEditor<M extends ItemMeta> {

    private static final Style DEFAULT_STYLE = Style.style().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();

    @Contract("-> new")
    public static @NotNull ItemEditor<ItemMeta> create() {
        return new ItemEditor<>(ItemMeta.class);
    }

    @Contract("_ -> new")
    public static <M extends ItemMeta> @NotNull ItemEditor<M> create(@NotNull Class<M> clazz) {
        return new ItemEditor<>(clazz);
    }

    private final Class<M> clazz;
    private @Nullable Component displayName;
    private @Nullable List<Component> lore;
    private Consumer<M> editMeta;

    private ItemEditor(@NotNull Class<M> clazz) {
        this.clazz = clazz;
    }

    public @NotNull ItemEditor<M> displayName(@NotNull Component displayName) {
        this.displayName = displayName.applyFallbackStyle(DEFAULT_STYLE);
        return this;
    }

    public @NotNull ItemEditor<M> clearLore() {
        this.lore = Collections.emptyList();
        return this;
    }

    public @NotNull ItemEditor<M> loreEmptyLine() {
        this.getOrCreateLore().add(Component.empty());
        return this;
    }

    public @NotNull ItemEditor<M> loreEmptyLineIf(boolean state) {
        if (state) {
            this.loreEmptyLine();
        }
        return this;
    }

    public @NotNull ItemEditor<M> loreLine(@NotNull Component line) {
        this.getOrCreateLore().add(line.applyFallbackStyle(DEFAULT_STYLE));
        return this;
    }

    public @NotNull ItemEditor<M> loreLineIf(boolean state, @NotNull Supplier<Component> line) {
        if (state) {
            this.loreLine(line.get());
        }
        return this;
    }

    public @NotNull ItemEditor<M> loreLines(@NotNull Component lines) {
        if (lines.children().isEmpty()) {
           return this.loreLine(lines);
        }

        var root = lines.children(Collections.emptyList());
        var builder = Component.text();

        builder.append(root.style(Style.empty()));

        for (var child : lines.children()) {
            if (child.equals(Component.newline())) {
                this.loreLine(builder.build().applyFallbackStyle(root.style()));
                builder = Component.text();
            } else {
                builder.append(child);
            }
        }

        this.loreLine(builder.build().applyFallbackStyle(root.style()));
        return this;
    }

    public @NotNull ItemEditor<M> loreLinesIf(boolean state, @NotNull Supplier<Component> lines) {
        if (state) {
            this.loreLines(lines.get());
        }
        return this;
    }

    public @NotNull ItemEditor<M> copyLoreFrom(@NotNull ItemStack source) {
        var lore = source.lore();
        if (lore != null) {
            this.getOrCreateLore().addAll(lore);
        }
        return this;
    }

    public @NotNull ItemEditor<M> editMeta(@NotNull Consumer<M> edit) {
        if (this.editMeta == null) {
            this.editMeta = edit;
        } else {
            this.editMeta = this.editMeta.andThen(edit);
        }
        return this;
    }

    @Contract("_ -> param1")
    public @NotNull ItemStack applyTo(@NotNull ItemStack item) {
        item.editMeta(meta -> {
            if (this.displayName != null) {
                meta.displayName(this.displayName);
            }
            if (this.lore != null) {
                meta.lore(this.lore);
            }
        });

        if (this.editMeta != null) {
            item.editMeta(this.clazz, this.editMeta);
        }

        return item;
    }

    public @NotNull ItemStack createItem(@NotNull Material material) {
        return this.createItem(material, 1);
    }

    public @NotNull ItemStack createItem(@NotNull Material material, int amount) {
        return this.applyTo(new ItemStack(material, amount));
    }

    private @NotNull List<Component> getOrCreateLore() {
        if (this.lore == null || this.lore == Collections.<Component>emptyList()) {
            this.lore = new ArrayList<>();
        }
        return this.lore;
    }
}
