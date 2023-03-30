package net.okocraft.box.api.util;

import org.jetbrains.annotations.ApiStatus;

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

    @ApiStatus.Experimental
    public static boolean check() {
        return RUNNING;
    }
}
