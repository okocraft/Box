package net.okocraft.box.plugin.locale.formatter;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Entity;
import net.md_5.bungee.api.chat.hover.content.Item;
import net.md_5.bungee.api.chat.hover.content.Text;
import net.okocraft.box.nms.adapter.ItemTagGenerator;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public final class ComponentGenerator {

    @NotNull
    public TextComponent generate(@NotNull String str, int index, @NotNull BaseComponent holder) {
        int pos = str.indexOf("{" + index + "}");

        TextComponent text = new TextComponent();

        if (-1 < pos) {
            text.addExtra(str.substring(0, pos));
            text.addExtra(holder);
            text.addExtra(str.substring(pos + 2));
        } else {
            text.setText(str);
        }

        return text;
    }

    @NotNull
    public TextComponent getHoverText(@NotNull String str, @NotNull String text) {
        TextComponent hoverText = new TextComponent(str);

        hoverText.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(text)));

        return hoverText;
    }

    @NotNull
    public TextComponent getHoverItem(@NotNull String str, @NotNull ItemStack show) {
        TextComponent hoverText = new TextComponent(str);

        hoverText.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ITEM,
                new Item(
                        show.getType().getKey().getKey(),
                        show.getAmount(),
                        ItemTagGenerator.generate(show)
                )
        ));

        return hoverText;
    }

    @NotNull
    public TextComponent getHoverEntity(@NotNull String str, @NotNull org.bukkit.entity.Entity entity) {
        TextComponent hoverText = new TextComponent(str);

        hoverText.setHoverEvent(new HoverEvent(
                HoverEvent.Action.SHOW_ENTITY,
                new Entity(
                        entity.getType().getKey().getKey(),
                        entity.getUniqueId().toString(),
                        new TextComponent(entity.getCustomName() != null ? entity.getCustomName() : entity.getName())
                )
        ));

        return hoverText;
    }
}
