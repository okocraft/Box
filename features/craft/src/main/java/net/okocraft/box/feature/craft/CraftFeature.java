package net.okocraft.box.feature.craft;

import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.feature.craft.loader.RecipeLoader;
import net.okocraft.box.feature.craft.mode.CraftMode;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;

public class CraftFeature extends AbstractBoxFeature {

    private final CraftMode craftMode = new CraftMode(RecipeLoader.load());

    public CraftFeature() {
        super("craft");
    }

    @Override
    public void enable() {
        ClickModeRegistry.register(craftMode);
    }

    @Override
    public void disable() {
        ClickModeRegistry.unregister(craftMode);
    }
}
