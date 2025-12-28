package net.okocraft.box.feature.gui.api.util;

import io.papermc.paper.datacomponent.DataComponentTypes;
import io.papermc.paper.datacomponent.item.ItemLore;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.translation.GlobalTranslator;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

public class ItemEditor {

    private static final Style DEFAULT_STYLE = Style.style().color(NamedTextColor.WHITE).decoration(TextDecoration.ITALIC, TextDecoration.State.FALSE).build();
    private static final Pattern LINE_SEPARATORS = Pattern.compile("\\r\\n|\\n|\\r");

    @Contract("-> new")
    public static @NotNull ItemEditor create() {
        return new ItemEditor();
    }

    private @Nullable ComponentLike displayName;
    private @Nullable List<ComponentLike> lore;
    private Consumer<PersistentDataContainer> editPersistentDataContainer;

    public @NotNull ItemEditor displayName(@NotNull ComponentLike displayName) {
        this.displayName = displayName;
        return this;
    }

    public @NotNull ItemEditor clearLore() {
        this.lore = null;
        return this;
    }

    public @NotNull ItemEditor loreEmptyLine() {
        this.getOrCreateLore().add(Component.empty());
        return this;
    }

    public @NotNull ItemEditor loreEmptyLineIf(boolean state) {
        if (state) {
            this.loreEmptyLine();
        }
        return this;
    }

    public @NotNull ItemEditor loreLine(@NotNull ComponentLike line) {
        this.getOrCreateLore().add(line);
        return this;
    }

    public @NotNull ItemEditor loreLineIf(boolean state, @NotNull Supplier<? extends ComponentLike> line) {
        if (state) {
            this.loreLine(line.get());
        }
        return this;
    }

    public @NotNull ItemEditor loreLines(@NotNull ComponentLike lines) {
        this.getOrCreateLore().add(new MultipleLineComponent(lines));
        return this;
    }

    public @NotNull ItemEditor copyLoreFrom(@NotNull ItemStack source) {
        var lore = source.lore();
        if (lore != null) {
            lore.forEach(this::loreLine);
        }
        return this;
    }

    public @NotNull ItemEditor editPersistentDataContainer(@NotNull Consumer<PersistentDataContainer> edit) {
        if (this.editPersistentDataContainer == null) {
            this.editPersistentDataContainer = edit;
        } else {
            this.editPersistentDataContainer = this.editPersistentDataContainer.andThen(edit);
        }
        return this;
    }

    @Contract("_, _ -> param2")
    public @NotNull ItemStack applyTo(@NotNull Player player, @NotNull ItemStack item) {
        if (this.displayName != null) {
            item.setData(DataComponentTypes.CUSTOM_NAME, renderComponent(player, this.displayName).applyFallbackStyle(DEFAULT_STYLE));
        }

        if (this.lore != null) {
            LoreBuilder builder = new LoreBuilder(player, this.lore.size());

            for (ComponentLike line : this.lore) {
                builder.processLine(line);
            }

            item.setData(DataComponentTypes.LORE, builder.toItemLore());
        }

        if (this.editPersistentDataContainer != null) {
            item.editPersistentDataContainer(this.editPersistentDataContainer);

        }

        return item;
    }

    public @NotNull ItemStack createItem(@NotNull Player player, @NotNull Material material) {
        return this.createItem(player, material, 1);
    }

    public @NotNull ItemStack createItem(@NotNull Player player, @NotNull Material material, int amount) {
        return this.applyTo(player, new ItemStack(material, amount));
    }

    private @NotNull List<ComponentLike> getOrCreateLore() {
        if (this.lore == null) {
            this.lore = new ArrayList<>();
        }
        return this.lore;
    }

    private static @NotNull Component renderComponent(@NotNull Player player, @NotNull ComponentLike component) {
        return GlobalTranslator.render(component.asComponent(), player.locale());
    }

    private record MultipleLineComponent(ComponentLike component) implements ComponentLike {
        @Override
        public @NotNull Component asComponent() {
            return this.component.asComponent();
        }
    }

    private static class LoreBuilder {

        private final Player player;
        private final List<Component> lines;
        private TextComponent.Builder builder = Component.text();

        private LoreBuilder(Player player, int initialCapacity) {
            this.player = player;
            this.lines = new ArrayList<>(initialCapacity);
        }

        private @NotNull ItemLore toItemLore() {
            return ItemLore.lore(this.lines);
        }

        private void processLine(ComponentLike line) {
            Component renderedLine = renderComponent(this.player, line);
            if (line instanceof MultipleLineComponent) {
                this.buildLines(renderedLine);
            } else {
                this.addLine(renderedLine);
            }
        }

        private void addLine(Component line) {
            this.lines.add(line.applyFallbackStyle(DEFAULT_STYLE));
        }

        private void appendComponent(@NotNull Component component) {
            if (this.builder == null) {
                this.builder = Component.text();
            }
            this.builder.append(component);
        }

        private void buildLines(@NotNull Component self) {
            this.splitContent(self);

            if (!self.children().isEmpty()) {
                for (Component child : self.children()) {
                    this.buildLines(child);
                }
            }

            if (this.builder != null) {
                this.addLine(this.builder.build());
                this.builder = null;
            }
        }

        private void splitContent(Component component) {
            if (!(component instanceof TextComponent text)) {
                this.appendComponent(component);
                return;
            }

            String[] lines = LINE_SEPARATORS.split(text.content());
            if (lines.length == 1) {
                this.appendComponent(Component.text(lines[0], text.style()));
            } else {
                for (String line : lines) {
                    this.appendComponent(Component.text(line, text.style()));
                    this.addLine(this.builder.build());
                    this.builder = null;
                }
            }
        }
    }
}
