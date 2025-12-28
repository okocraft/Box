package net.okocraft.box.feature.autostore;

import dev.siroshun.mcmsgdef.MessageKey;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.AutoSaveListener;
import net.okocraft.box.feature.autostore.listener.CustomDataExportListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

/**
 * A {@link net.okocraft.box.api.feature.BoxFeature} that provides autostore feature.
 */
public class AutoStoreFeature extends AbstractBoxFeature {

    private static final Key AUTO_SAVE_LISTENER_KEY = Key.key("box", "feature/autostore/auto_save_listener");
    private static final Key CUSTOM_DATA_EXPORT_LISTENER_KEY = Key.key("box", "feature/autostore/custom_data_export_listener");

    private final AutoStoreSettingContainer settingContainer;
    private final MessageKey loadErrorMessage;

    private final AutoSaveListener autoSaveListener;
    private final CustomDataExportListener customDataExportListener;
    private final ItemListener itemListener;

    private final AutoStoreCommand autoStoreCommand;
    private final AutoStoreClickMode autoStoreClickMode;

    /**
     * The constructor of {@link AutoStoreFeature}.
     *
     * @param context a context of {@link net.okocraft.box.api.feature.FeatureContext.Registration}
     */
    @ApiStatus.Internal
    public AutoStoreFeature(@NotNull FeatureContext.Registration context) {
        super("autostore");
        var collector = context.defaultMessageCollector();
        this.loadErrorMessage = MessageKey.key(collector.add("box.autostore.error.failed-to-load-settings", "<red>Failed to load the auto-store settings. Please contact the administrator."));
        this.settingContainer = new AutoStoreSettingContainer();
        this.autoSaveListener = new AutoSaveListener(this.settingContainer);
        this.customDataExportListener = new CustomDataExportListener();
        this.itemListener = new ItemListener(this.settingContainer);
        this.autoStoreCommand = new AutoStoreCommand(this.settingContainer, this.loadErrorMessage, collector);
        this.autoStoreClickMode = new AutoStoreClickMode(this.settingContainer, collector);
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        this.settingContainer.registerBoxPlayerListener(this.loadErrorMessage);
        this.autoSaveListener.register(AUTO_SAVE_LISTENER_KEY);
        this.customDataExportListener.register(CUSTOM_DATA_EXPORT_LISTENER_KEY, AutoStoreSettingContainer::onExportAutoStoreSetting);

        Bukkit.getPluginManager().registerEvents(this.itemListener, context.plugin());

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(this.autoStoreCommand);
        ClickModeRegistry.register(this.autoStoreClickMode);

        for (var player : Bukkit.getOnlinePlayers()) {
            this.settingContainer.load(player, this.loadErrorMessage);
        }
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(this.autoStoreCommand);
        ClickModeRegistry.unregister(this.autoStoreClickMode);

        HandlerList.unregisterAll(this.itemListener);

        this.autoSaveListener.unregister();
        this.customDataExportListener.unregister();
        this.settingContainer.unregisterBoxPlayerListener();

        this.settingContainer.unloadAll();
    }

    /**
     * Gets a {@link AutoStoreSettingProvider}.
     *
     * @return a {@link AutoStoreSettingProvider}
     */
    @SuppressWarnings("unused")
    public @NotNull AutoStoreSettingProvider getAutoStoreSettingProvider() {
        return this.settingContainer;
    }
}
