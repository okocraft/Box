package net.okocraft.box.feature.craft;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.api.util.BoxLogger;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.lang.DisplayKeys;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.craft.model.RecipeHolder;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

public class CraftFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final CraftMode craftMode;
    private final CraftCommand craftCommand;
    private final MiniMessageBase reloaded;

    public CraftFeature(@NotNull FeatureContext.Registration context) {
        super("craft");
        DisplayKeys.addDefaults(context.defaultMessageCollector());
        this.craftMode = new CraftMode(context.defaultMessageCollector());
        this.craftCommand = new CraftCommand(context.defaultMessageCollector());
        this.reloaded = MiniMessageBase.messageKey(context.defaultMessageCollector().add("box.craft.reloaded", "<gray>Item recipes have been reloaded."));
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
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
    public void disable(@NotNull FeatureContext.Disabling context) {
        ClickModeRegistry.unregister(craftMode);
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(craftCommand);
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) {
        disable(context.asDisabling());
        enable(context.asEnabling());
        this.reloaded.source(BoxAPI.api().getMessageProvider().findSource(context.commandSender())).send(context.commandSender());
    }

    @Override
    public @NotNull @Unmodifiable Set<Class<? extends BoxFeature>> getDependencies() {
        return Set.of(GuiFeature.class);
    }
}
