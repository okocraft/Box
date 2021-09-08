package net.okocraft.box.bundle;

import net.okocraft.box.api.feature.BoxFeature;
import net.okocraft.box.autostore.AutoStoreFeature;
import net.okocraft.box.category.CategoryFeature;
import net.okocraft.box.command.CommandFeature;
import net.okocraft.box.stick.StickFeature;

import java.util.List;

final class Bundled {

    static final List<BoxFeature> FEATURES =
            List.of(new CategoryFeature(), new AutoStoreFeature(), new CommandFeature(), new StickFeature());
}
