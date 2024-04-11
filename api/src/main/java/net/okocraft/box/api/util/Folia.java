package net.okocraft.box.api.util;

import org.jetbrains.annotations.ApiStatus;

/**
 * A helper class for checking if the server software is Folia.
 */
@ApiStatus.Experimental
public final class Folia {

    private static final boolean RUNNING;

    static {
        boolean isFolia;

        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServerInitEvent");
            isFolia = true;
        } catch (ClassNotFoundException e) {
            isFolia = false;
        }

        RUNNING = isFolia;
    }

    /**
     * Returns whether the server software is Folia.
     *
     * @return {@code true} if the server software is Folia, otherwise {@code false}
     */
    @ApiStatus.Experimental
    public static boolean check() {
        return RUNNING;
    }

    private Folia() {
        throw new UnsupportedOperationException();
    }
}
