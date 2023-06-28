package net.okocraft.box.version.common.item;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface DefaultItemProvider {

    int version();

    @NotNull List<DefaultItem> provide();

}
