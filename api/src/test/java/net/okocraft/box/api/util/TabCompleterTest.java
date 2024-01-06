package net.okocraft.box.api.util;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;

class TabCompleterTest {

    private static final List<String> COMPLETIONS = List.of("dirt", "stone", "glass", "diamond");

    @Test
    void testEmpty() {
        Assertions.assertEquals(COMPLETIONS,  filterElements(""));
    }

    @Test
    void testFilter() {
        Assertions.assertEquals(List.of("dirt", "diamond"), filterElements("d"));
        Assertions.assertEquals(List.of("dirt"), filterElements("dir"));

        // Checks if the same result can be obtained even prefix is uppercase.
        Assertions.assertEquals(List.of("dirt", "diamond"), filterElements("D"));
        Assertions.assertEquals(List.of("dirt"), filterElements("dIR"));
    }

    private static @NotNull List<String> filterElements(@NotNull String prefix) {
        return COMPLETIONS.stream().filter(name -> TabCompleter.startsWith(name, prefix)).toList();
    }
}
