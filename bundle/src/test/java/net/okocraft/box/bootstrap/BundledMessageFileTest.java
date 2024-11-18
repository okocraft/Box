package net.okocraft.box.bootstrap;

import net.okocraft.box.bundle.Builtin;
import net.okocraft.box.core.message.CoreMessages;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Map;

class BundledMessageFileTest {

    @Test
    void testJapaneseFile(@TempDir Path dir) {
        test(dir, Builtin.loadJapaneseFileFromJar());
    }

    private static void test(@NotNull Path dir, @NotNull Map<String, String> messageMap) {
        var context = new BoxBootstrapContext(dir, BundledMessageFileTest.class.getPackage().getImplementationVersion());

        CoreMessages.addDefaultMessages(context.getDefaultMessageCollector());
        Builtin.features(context);

        var collected = context.getDefaultMessageCollector().getCollectedMessages();

        for (var key : messageMap.keySet()) {
            Assertions.assertNotNull(collected.remove(key), "Unknown key: " + key);
        }

        if (!collected.isEmpty()) {
            Assertions.fail("Missing keys: " + String.join(", ", collected.keySet()));
        }
    }
}
