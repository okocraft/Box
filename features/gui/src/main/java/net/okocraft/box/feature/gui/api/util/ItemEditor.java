package net.okocraft.box.feature.gui.api.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ItemEditor<M extends ItemMeta> {

    private static final Style DEFAULT_STYLE = Style.style().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();
    private static final Pattern LINE_SEPARATORS = Pattern.compile("\\r\\n|\\n|\\r");

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
    private @Nullable ItemLore.Builder lore;
    private Consumer<M> editMeta;

    private ItemEditor(@NotNull Class<M> clazz) {
        this.clazz = clazz;
    }

    public @NotNull ItemEditor<M> displayName(@NotNull Component displayName) {
        this.displayName = displayName.applyFallbackStyle(DEFAULT_STYLE);
        return this;
    }

    public @NotNull ItemEditor<M> clearLore() {
        this.lore = ItemLore.lore();
        return this;
    }

    public @NotNull ItemEditor<M> loreEmptyLine() {
        this.getOrCreateLore().addLine(Component.empty());
        return this;
    }

    public @NotNull ItemEditor<M> loreEmptyLineIf(boolean state) {
        if (state) {
            this.loreEmptyLine();
        }
        return this;
    }

    public @NotNull ItemEditor<M> loreLine(@NotNull Component line) {
        this.getOrCreateLore().addLine(line.applyFallbackStyle(DEFAULT_STYLE));
        return this;
    }

    public @NotNull ItemEditor<M> loreLineIf(boolean state, @NotNull Supplier<Component> line) {
        if (state) {
            this.loreLine(line.get());
        }
        return this;
    }

    public @NotNull ItemEditor<M> loreLines(@NotNull Component lines) {
        var builder = new LineBuilder();

        this.buildLines(lines, builder);

        if (builder.hasBuildingLine()) {
            builder.buildLine(this::loreLine);
        }

        return this;
    }

    private void buildLines(@NotNull Component self, @NotNull ItemEditor.LineBuilder builder) {
        this.splitContent(self, builder);

        if (!self.children().isEmpty()) {
            for (Component child : self.children()) {
                this.buildLines(child, builder);
            }
        }
    }

    private void splitContent(Component component, @NotNull ItemEditor.LineBuilder builder) {
        if (!(component instanceof TextComponent text)) {
            builder.appendComponent(component);
            return;
        }

        var lines = LINE_SEPARATORS.split(text.content());

        if (lines.length == 1) {
            builder.appendComponent(Component.text(lines[0], text.style()));
        } else {
            for (var line : lines) {
                builder.appendComponent(Component.text(line, text.style()));
                builder.buildLine(this::loreLine);
            }
        }
    }

    public @NotNull ItemEditor<M> copyLoreFrom(@NotNull ItemStack source) {
        var lore = source.lore();
        if (lore != null) {
            this.getOrCreateLore().addLines(lore);
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
        if (this.displayName != null) {
            item.setData(DataComponentTypes.CUSTOM_NAME, this.displayName);
        }

        if (this.lore != null) {
            item.setData(DataComponentTypes.LORE, this.lore);
        }

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

    private @NotNull ItemLore.Builder getOrCreateLore() {
        if (this.lore == null) {
            this.lore = ItemLore.lore();
        }
        return this.lore;
    }

    private static class LineBuilder {

        private TextComponent.Builder builder = Component.text();

        private void appendComponent(@NotNull Component component) {
            if (this.builder == null) {
                this.builder = Component.text();
            }
            this.builder.append(component);
        }

        private boolean hasBuildingLine() {
            return this.builder != null;
        }

        private void buildLine(@NotNull Consumer<Component> consumer) {
            consumer.accept(Objects.requireNonNull(this.builder).build());
            this.builder = null;
        }
    }
}
