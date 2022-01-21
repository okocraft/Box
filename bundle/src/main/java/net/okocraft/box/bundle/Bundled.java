package net.okocraft.box.bundle;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.bemode.BEModeFeature;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.command.CommandFeature;
import net.okocraft.box.feature.craft.CraftFeature;
import net.okocraft.box.feature.gui.GuiFeature;
import net.okocraft.box.feature.notifier.NotifierFeature;
import net.okocraft.box.feature.stick.StickFeature;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;

final class Bundled {

    private static final List<BoxFeature> FEATURES =
            List.of(new CommandFeature(), new CategoryFeature(), new GuiFeature(),
                    new BEModeFeature(), new AutoStoreFeature(), new CraftFeature(),
                    new StickFeature(), new NotifierFeature());

    static @NotNull @Unmodifiable List<BoxFeature> features() {
        return FEATURES;
    }
}
