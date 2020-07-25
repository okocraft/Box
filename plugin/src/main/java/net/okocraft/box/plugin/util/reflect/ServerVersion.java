package net.okocraft.box.plugin.util.reflect;

import org.bukkit.Bukkit;

public enum ServerVersion {
    v1_15_R1,
    v1_16_R1,
    UNKNOWN;

    private static ServerVersion VERSION;

    public static ServerVersion get() {
        if (VERSION == null) {
            VERSION = getVersion();
        }

        return VERSION;
    }

    private static ServerVersion getVersion() {
        switch (Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3]) {
            case "v1_15_R1":
                return v1_15_R1;
            case "v1_16_R1":
                return v1_16_R1;
            default:
                return UNKNOWN;
        }
    }
}
