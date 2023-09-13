package net.okocraft.box.feature.craft;

import com.github.siroshun09.configapi.api.util.ResourceUtils;
import com.github.siroshun09.configapi.yaml.YamlConfiguration;
import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.api.feature.Disableable;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.api.message.Components;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model.IngredientHolder;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.io.IOException;
import java.util.Set;
import java.util.logging.Level;

public class CraftFeature extends AbstractBoxFeature implements Disableable, Reloadable {

    private final CraftMode craftMode = new CraftMode();
    private final CraftCommand craftCommand = new CraftCommand();

    public CraftFeature() {
        super("craft");
    }

    @Override
    public void enable() {
        var recipeFile = BoxProvider.get().getPluginDirectory().resolve("recipes.yml");

        var recipeConfig = YamlConfiguration.create(recipeFile);

        try {
            ResourceUtils.copyFromJarIfNotExists(BoxProvider.get().getJar(), "recipes.yml", recipeConfig.getPath());
            recipeConfig.load();
        } catch (IOException e) {
            BoxProvider.get().getLogger().log(Level.SEVERE, "Could not load recipes.yml", e);
        }

        // Reduce objects that will be generated
        // BoxIngredientItem 5030 -> 325
        // IngredientHolder 3506 -> 1417
        IngredientHolder.enableCache();

        var recipeMap = RecipeLoader.load(recipeConfig);

        IngredientHolder.disableCache();

        RecipeRegistry.setRecipeMap(recipeMap);

        BoxProvider.get().getLogger().info(recipeMap.size() + " recipes are imported!");

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
