package net.okocraft.box.feature.craft;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class CraftFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final CraftMode craftMode = new CraftMode();
    private final CraftCommand craftCommand = new CraftCommand();

    public CraftFeature() {
        super("craft");
    }

    @Override
    public void enable() {
        RecipeRegistry.setRecipeMap(RecipeLoader.load());
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

        try {
            sender.sendMessage(Component.translatable("box.craft.command.recipe-reloaded", NamedTextColor.GRAY));
        } catch (Exception ignored) {
            // I don't know why it loops infinitely and throws an exception when the message send to the console.
            // It's probably a bug of Paper or Adventure.
            //
            // IllegalStateException: Exceeded maximum depth of 512 while attempting to flatten components!
        }
    }
}
