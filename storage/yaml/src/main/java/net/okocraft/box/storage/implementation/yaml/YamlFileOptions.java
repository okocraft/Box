package net.okocraft.box.storage.implementation.yaml;

import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

final class YamlFileOptions {

    static final OpenOption[] WRITE = new OpenOption[]{
        StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING
    };

    private YamlFileOptions() {
        throw new UnsupportedOperationException();
    }
}
