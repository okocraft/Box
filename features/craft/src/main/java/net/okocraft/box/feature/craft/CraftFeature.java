package net.okocraft.box.feature.craft;

import net.okocraft.box.api.BoxProvider;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.model.item.BoxItem;
import net.okocraft.box.feature.craft.command.CraftCommand;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.craft.model2.RecipeHolder;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;

import java.util.Map;

public class CraftFeature extends AbstractBoxFeature {

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
}
