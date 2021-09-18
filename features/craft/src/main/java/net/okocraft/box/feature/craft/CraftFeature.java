package net.okocraft.box.feature.craft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class CraftFeature extends AbstractBoxFeature implements Reloadable {

    private final Map<BoxItem, RecipeHolder> recipeMap = RecipeLoader.load();
    private final CraftMode craftMode = new CraftMode(recipeMap);
    private final CraftCommand craftCommand = new CraftCommand(recipeMap);

    public CraftFeature() {
        super("craft");
    }

    @Override
    public void enable() {
        ClickModeRegistry.register(craftMode);
        BoxProvider.get().getBoxCommand().getSubCommandHolder().register(craftCommand);
    }

    @Override
    public void disable() {
        ClickModeRegistry.unregister(craftMode);
        BoxProvider.get().getBoxCommand().getSubCommandHolder().unregister(craftCommand);
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();
        sender.sendMessage(Component.translatable("box.craft.command.recipe-reloaded", NamedTextColor.GRAY));
    }
}
