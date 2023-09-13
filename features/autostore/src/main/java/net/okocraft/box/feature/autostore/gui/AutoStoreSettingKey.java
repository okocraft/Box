package net.okocraft.box.feature.autostore.gui;

import net.okocraft.box.feature.autostore.model.setting.AutoStoreSetting;
import net.okocraft.box.feature.gui.api.session.TypedKey;

public final class AutoStoreSettingKey {

    public static final TypedKey<AutoStoreSetting> KEY = new TypedKey<>(AutoStoreSetting.class, "autostore_setting");

}
