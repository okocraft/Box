package net.okocraft.box.item;

import net.okocraft.box.api.util.MCDataVersion;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

public interface DefaultItemProvider {

    static @NotNull DefaultItemProvider createDefaultItemProvider() {
        return new DefaultItemProviderImpl();
    }

    @NotNull Stream<DefaultItem> provide();

    @NotNull
    Map<String, String> renamedItems(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion);

    @NotNull UnaryOperator<String> itemNameConvertor(@NotNull MCDataVersion startingVersion, @NotNull MCDataVersion currentVersion);

}
