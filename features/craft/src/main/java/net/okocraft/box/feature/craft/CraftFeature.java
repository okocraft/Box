package net.okocraft.box.feature.craft;

import dev.siroshun.mcmsgdef.MessageKey;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Set;

public class CraftFeature extends AbstractBoxFeature implements Reloadable {

    private final CraftMode craftMode;
    private final CraftCommand craftCommand;
    private final MessageKey reloaded;

    public CraftFeature(@NotNull FeatureContext.Registration context) {
        super("craft");
        DisplayKeys.addDefaults(context.defaultMessageCollector());
        this.craftMode = new CraftMode(context.defaultMessageCollector());
        this.craftCommand = new CraftCommand(context.defaultMessageCollector());
        this.reloaded = MessageKey.key(context.defaultMessageCollector().add("box.craft.reloaded", "<gray>Item recipes have been reloaded."));
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) throws IOException {
        // Reduce objects that will be generated
        // BoxIngredientItem 5030 -> 325
        // IngredientHolder 3506 -> 1417
        IngredientHolder.enableCache();

        var recipeMap = RecipeLoader.load(BoxAPI.api().getPluginDirectory().resolve("recipes.yml"));

        IngredientHolder.disableCache();

        RecipeRegistry.setRecipeMap(recipeMap);

        BoxLogger.logger().info("{} recipes are imported!", recipeMap.size());

        ClickModeRegistry.register(this.craftMode);

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(this.craftCommand);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        ClickModeRegistry.unregister(this.craftMode);
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(this.craftCommand);
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) throws IOException {
        this.disable(context.asDisabling());
        this.enable(context.asEnabling());
        context.commandSender().sendMessage(this.reloaded);
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
