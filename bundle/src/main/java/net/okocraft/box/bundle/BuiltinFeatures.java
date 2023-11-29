package net.okocraft.box.bundle;

import net.okocraft.box.bootstrap.BoxBootstrapContext;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.bemode.BEModeFeature;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.command.CommandFeature;
import net.okocraft.box.feature.craft.CraftFeature;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.notifier.NotifierFeature;
import net.okocraft.box.feature.stick.StickFeature;
import org.jetbrains.annotations.NotNull;

public final class BuiltinFeatures {

    public static void addToContext(@NotNull BoxBootstrapContext context) {
        context.addFeature(CommandFeature::new)
                .addFeature(CategoryFeature::new)
                .addFeature(GuiFeature::new)
                .addFeature(BEModeFeature::new)
                .addFeature(AutoStoreFeature::new)
                .addFeature(CraftFeature::new)
                .addFeature(StickFeature::new)
                .addFeature(NotifierFeature::new);
    }

    private BuiltinFeatures() {
        throw new UnsupportedOperationException();
    }
}
