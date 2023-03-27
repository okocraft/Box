package net.okocraft.box.util;

import com.github.siroshun09.translationloader.TranslationLoader;
import com.github.siroshun09.translationloader.directory.TranslationDirectory;
import com.github.siroshun09.translationloader.directory.TranslationLoaderCreator;
import com.github.siroshun09.translationloader.util.PathConsumer;
import net.kyori.adventure.key.Key;
import net.okocraft.box.bootstrap.BootstrapContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TranslationDirectoryUtil {

    public static @NotNull PathConsumerWrapper createPathConsumer() {
        return new PathConsumerWrapper();
    }

    public static @NotNull TranslationLoaderCreatorHolder createCreatorHolder() {
        return new TranslationLoaderCreatorHolder();
    }

    public static @NotNull TranslationDirectory fromContext(@NotNull BootstrapContext context) {
        return TranslationDirectory.newBuilder()
                .setDirectory(context.getPluginDirectory().resolve("languages"))
                .setKey(Key.key("box", "language"))
                .setDefaultLocale(Locale.ENGLISH)
                .onDirectoryCreated(context.onLanguageDirectoryCreated())
                .setVersion(context.getVersion())
                .setTranslationLoaderCreator(context.getTranslationLoaderCreators())
                .build();
    }

    public static class PathConsumerWrapper implements PathConsumer {

        private final List<PathConsumer> consumers = new ArrayList<>();

        private PathConsumerWrapper() {
        }

        @Override
        public void accept(@NotNull Path path) throws IOException {
            for (var consumer : consumers) {
                consumer.accept(path);
            }
        }

        public void add(@NotNull PathConsumer consumer) {
            consumers.add(consumer);
        }
    }

    public static class TranslationLoaderCreatorHolder implements TranslationLoaderCreator {

        private final List<TranslationLoaderCreator> creators = new ArrayList<>();

        private TranslationLoaderCreatorHolder() {
        }

        @Override
        public @Nullable TranslationLoader createLoader(@NotNull Locale locale) throws IOException {
            TranslationLoader result = null;

            for (var creator : creators) {
                result = creator.createLoader(locale);

                if (result != null) {
                    break;
                }
            }

            return result;
        }

        public void addCreator(@NotNull TranslationLoaderCreator creator) {
            creators.add(creator);
        }
    }
}
