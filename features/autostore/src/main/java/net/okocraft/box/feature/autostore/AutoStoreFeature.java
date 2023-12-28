package net.okocraft.box.feature.autostore;

import com.github.siroshun09.messages.minimessage.base.MiniMessageBase;
import net.kyori.adventure.key.Key;
import net.okocraft.box.api.BoxAPI;
import net.okocraft.box.api.feature.AbstractBoxFeature;
import net.okocraft.box.api.feature.FeatureContext;
import net.okocraft.box.api.feature.Reloadable;
import net.okocraft.box.feature.autostore.command.AutoStoreCommand;
import net.okocraft.box.feature.autostore.gui.AutoStoreClickMode;
import net.okocraft.box.feature.autostore.listener.AutoSaveListener;
import net.okocraft.box.feature.autostore.listener.BoxPlayerListener;
import net.okocraft.box.feature.autostore.listener.ItemListener;
import net.okocraft.box.feature.autostore.model.AutoStoreSettingContainer;
import net.okocraft.box.feature.gui.api.mode.ClickModeRegistry;
import org.jetbrains.annotations.NotNull;

import static com.github.siroshun09.messages.minimessage.base.MiniMessageBase.messageKey;

public class AutoStoreFeature extends AbstractBoxFeature implements Reloadable {

    public static final @NotNull Key AUTO_SAVE_LISTENER_KEY = Key.key("box", "feature/autostore/auto_save_listener");
    public static final @NotNull Key PLAYER_LISTENER_KEY = Key.key("box", "feature/autostore/player_listener");

    private final AutoStoreSettingContainer settingContainer;
    private final MiniMessageBase reloadSuccess;

    private final BoxPlayerListener boxPlayerListener;
    private final AutoSaveListener autoSaveListener;
    private final ItemListener itemListener;

    private final AutoStoreCommand autoStoreCommand;
    private final AutoStoreClickMode autoStoreClickMode;

    public AutoStoreFeature(@NotNull FeatureContext.Registration context) {
        super("autostore");
        var collector = context.defaultMessageCollector();
        this.settingContainer = new AutoStoreSettingContainer(messageKey(collector.add("box.autostore.error.failed-to-load-settings", "<red>Failed to load the auto-store settings. Please contact the administrator.")));
        this.reloadSuccess = messageKey(collector.add("box.autostore.reload-success", "<gray>Auto store feature has been reloaded."));
        this.boxPlayerListener = new BoxPlayerListener(this.settingContainer);
        this.autoSaveListener = new AutoSaveListener(this.settingContainer);
        this.itemListener = new ItemListener(this.settingContainer);
        this.autoStoreCommand = new AutoStoreCommand(this.settingContainer, collector);
        this.autoStoreClickMode = new AutoStoreClickMode(this.settingContainer, collector);
    }

    @Override
    public void enable(@NotNull FeatureContext.Enabling context) {
        this.settingContainer.loadAll();

        this.boxPlayerListener.register(PLAYER_LISTENER_KEY);
        this.autoSaveListener.register(AUTO_SAVE_LISTENER_KEY);

        this.itemListener.register();

        BoxAPI.api().getBoxCommand().getSubCommandHolder().register(this.autoStoreCommand);
        ClickModeRegistry.register(this.autoStoreClickMode);
    }

    @Override
    public void disable(@NotNull FeatureContext.Disabling context) {
        BoxAPI.api().getBoxCommand().getSubCommandHolder().unregister(this.autoStoreCommand);
        ClickModeRegistry.unregister(this.autoStoreClickMode);

        this.itemListener.unregister();

        this.autoSaveListener.unregister(AUTO_SAVE_LISTENER_KEY);
        this.boxPlayerListener.unregister(PLAYER_LISTENER_KEY);

        this.settingContainer.unloadAll();
    }

    @Override
    public void reload(@NotNull FeatureContext.Reloading context) {
        this.disable(context.asDisabling());
        this.enable(context.asEnabling());
        this.reloadSuccess.source(BoxAPI.api().getMessageProvider().findSource(context.commandSender())).send(context.commandSender());
    }
}
