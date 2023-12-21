package net.okocraft.box.feature.craft;

import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CraftFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final CraftMode craftMode = new CraftMode();
    private final CraftCommand craftCommand = new CraftCommand();

    public CraftFeature() {
        super("craft");
    }

    @Override
    public void enable() {
        // Reduce objects that will be generated
        // BoxIngredientItem 5030 -> 325
        // IngredientHolder 3506 -> 1417
        IngredientHolder.enableCache();

        Map<BoxItem, RecipeHolder> recipeMap;

        try {
            recipeMap = RecipeLoader.load(BoxAPI.api().getPluginDirectory().resolve("recipes.yml"));
        } catch (IOException e) {
            BoxLogger.logger().error("Could not load recipes.yml", e);
            return;
        }

        IngredientHolder.disableCache();

        RecipeRegistry.setRecipeMap(recipeMap);

        BoxLogger.logger().info("{} recipes are imported!", recipeMap.size());

        ClickModeRegistry.register(craftMode);

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(craftCommand);
    }

    @Override
    public void disable() {
        ClickModeRegistry.unregister(craftMode);
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(craftCommand);
    }

    @Override
    public void reload(@NotNull CommandSender sender) {
        disable();
        enable();

        try {
            sender.sendMessage(Components.grayTranslatable("box.craft.command.recipe-reloaded"));
        } catch (Exception ignored) {
            // I don't know why it loops infinitely and throws an exception when the message send to the console.
            // It's probably a bug of Paper or Adventure.
            //
            // IllegalStateException: Exceeded maximum depth of 512 while attempting to flatten components!
        }
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
