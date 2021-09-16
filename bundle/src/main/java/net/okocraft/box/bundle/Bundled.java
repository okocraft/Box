package net.okocraft.box.bundle;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.feature.autostore.AutoStoreFeature;
import net.okocraft.box.feature.category.CategoryFeature;
import net.okocraft.box.feature.command.CommandFeature;
import net.okocraft.box.feature.stick.StickFeature;
import net.okocraft.box.gui.GuiFeature;

import java.util.List;

final class Bundled {

    static final List<BoxFeature> FEATURES =
            List.of(new CategoryFeature(), new AutoStoreFeature(), new CommandFeature(),
                    new GuiFeature(), new StickFeature());
}
