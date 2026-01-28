package net.okocraft.box.core.message;

import dev.siroshun.mcmsgdef.directory.DirectorySource;
import dev.siroshun.mcmsgdef.directory.MessageProcessors;
import dev.siroshun.mcmsgdef.file.Loader;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.minimessage.translation.MiniMessageTranslationStore;
import net.kyori.adventure.translation.GlobalTranslator;
import net.okocraft.box.api.message.DefaultMessageCollector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.TestOnly;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public class BoxMessageProvider {

    public static @NotNull BoxMessageProvider.Collector createCollector() {
        return new BoxMessageProvider.Collector();
    }

    private final Collector collector;
    private final DirectorySource directorySource;
    private final Loader<Locale, Map<String, String>> defaultMessageMapLoader;

    private MiniMessageTranslationStore store;

    public BoxMessageProvider(@NotNull Collector collector, DirectorySource directorySource, Loader<Locale, Map<String, String>> defaultMessageMapLoader) {
        this.collector = collector;
        this.directorySource = directorySource;
        this.defaultMessageMapLoader = defaultMessageMapLoader;
    }

    public void load() throws IOException {
        if (this.store != null) {
            GlobalTranslator.translator().removeSource(this.store);
        }

        this.store = this.directorySource
            .defaultLocale(Locale.ENGLISH)
            .messageProcessor(MessageProcessors.appendMissingMessagesToPropertiesFile(this.defaultMessageMapLoader))
            .messageProcessor(MessageProcessors.appendMissingMessagesToPropertiesFile(ignored -> this.collector.collectedMessages))
            .loadAsMiniMessageTranslationStore(Key.key("box", "languages"));
        GlobalTranslator.translator().addSource(this.store);
    }

    public void unload() {
        if (this.store != null) {
            GlobalTranslator.translator().removeSource(this.store);
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
