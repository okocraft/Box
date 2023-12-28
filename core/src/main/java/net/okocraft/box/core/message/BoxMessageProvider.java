package net.okocraft.box.core.message;

import com.github.siroshun09.messages.api.directory.DirectorySource;
import com.github.siroshun09.messages.api.directory.LoadedMessageSource;
import com.github.siroshun09.messages.api.source.StringMessageMap;
import com.github.siroshun09.messages.api.util.Loader;
import com.github.siroshun09.messages.api.util.PropertiesFile;
import com.github.siroshun09.messages.minimessage.localization.MiniMessageLocalization;
import com.github.siroshun09.messages.minimessage.source.MiniMessageSource;
import net.okocraft.box.api.message.DefaultMessageCollector;
import net.okocraft.box.api.message.MessageProvider;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class BoxMessageProvider implements MessageProvider {

    public static @NotNull BoxMessageProvider.Collector createCollector() {
        return new BoxMessageProvider.Collector();
    }

    private final Collector collector;
    private final DirectorySource<StringMessageMap> directorySource;
    private final Loader<Locale, Map<String, String>> defaultMessageMapLoader;

    private MiniMessageLocalization localization;

    public BoxMessageProvider(@NotNull Collector collector, DirectorySource<StringMessageMap> directorySource, Loader<Locale, Map<String, String>> defaultMessageMapLoader) {
        this.collector = collector;
        this.directorySource = directorySource;
        this.defaultMessageMapLoader = defaultMessageMapLoader;
    }

    @Override
    public @NotNull MiniMessageSource findSource(@NotNull CommandSender sender) {
        return this.localization.findSource(sender instanceof Player player ? player.locale() : Locale.getDefault());
    }

    public void load() throws IOException {
        if (this.localization == null) { // on startup
            this.localization = new MiniMessageLocalization(MiniMessageSource.create(StringMessageMap.create(this.collector.collectedMessages)));
        } else { // on reload
            this.localization.clearSources();
        }

        this.directorySource.load(this::processLoadedMessages);
    }

    public void unload() {
        if (this.localization != null) {
            this.localization.clearSources();
        }
    }

    private @Nullable Void processLoadedMessages(@NotNull LoadedMessageSource<StringMessageMap> loadedSource) throws IOException {
        var locale = loadedSource.locale();

        var defaultMessageMap = this.defaultMessageMapLoader.load(locale);

        if (defaultMessageMap != null) {
            putMissingMessages(loadedSource, defaultMessageMap);
        }

        putMissingMessages(loadedSource, this.collector.collectedMessages);

        this.localization.addSource(locale, MiniMessageSource.create(loadedSource.messageSource()));
        return null;
    }

    private static void putMissingMessages(@NotNull LoadedMessageSource<StringMessageMap> loadedSource, Map<String, String> defaultMessageMap) throws IOException {
        var missingMessages = loadedSource.messageSource().mergeAndCollectMissingMessages(defaultMessageMap);

        if (!missingMessages.isEmpty()) {
            PropertiesFile.append(loadedSource.filepath(), missingMessages);
        }
    }

    public static class Collector implements DefaultMessageCollector {

        private final Map<String, String> collectedMessages = new LinkedHashMap<>();

        private Collector() {
        }

        @Override
        @Contract("_, _ -> param1")
        public @NotNull String add(@NotNull String key, @NotNull String defaultMessage) {
            if (this.collectedMessages.put(key, defaultMessage) != null) {
                throw new IllegalArgumentException(key + " is already defined.");
            }

            return key;
        }

        @TestOnly
        public @NotNull Map<String, String> getCollectedMessages() {
            return new LinkedHashMap<>(this.collectedMessages);
        }
    }
}
