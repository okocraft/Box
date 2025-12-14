package net.okocraft.box.api.util;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

/**
 * A class to get/compare Minecraft versions based on data version.
 *
 * @param dataVersion the data version (<a href="https://minecraft.wiki/w/Data_version">Minecraft Wiki: Data version</a>)
 */
@SuppressWarnings("unused")
public record MCDataVersion(int dataVersion) implements Version<MCDataVersion> {

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.17
     */
    public static final MCDataVersion MC_1_17 = new MCDataVersion(2724);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.17.1
     */
    public static final MCDataVersion MC_1_17_1 = new MCDataVersion(2730);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18
     */
    public static final MCDataVersion MC_1_18 = new MCDataVersion(2860);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18.1
     */
    public static final MCDataVersion MC_1_18_1 = new MCDataVersion(2865);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.18.2
     */
    public static final MCDataVersion MC_1_18_2 = new MCDataVersion(2975);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19
     */
    public static final MCDataVersion MC_1_19 = new MCDataVersion(3105);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.1
     */
    public static final MCDataVersion MC_1_19_1 = new MCDataVersion(3117);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.2
     */
    public static final MCDataVersion MC_1_19_2 = new MCDataVersion(3120);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.3
     */
    public static final MCDataVersion MC_1_19_3 = new MCDataVersion(3218);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.19.4
     */
    public static final MCDataVersion MC_1_19_4 = new MCDataVersion(3337);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20
     */
    public static final MCDataVersion MC_1_20 = new MCDataVersion(3463);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.1
     */
    public static final MCDataVersion MC_1_20_1 = new MCDataVersion(3465);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.2
     */
    public static final MCDataVersion MC_1_20_2 = new MCDataVersion(3578);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.3
     */
    public static final MCDataVersion MC_1_20_3 = new MCDataVersion(3698);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.4
     */
    public static final MCDataVersion MC_1_20_4 = new MCDataVersion(3700);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.5
     */
    public static final MCDataVersion MC_1_20_5 = new MCDataVersion(3837);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.20.6
     */
    public static final MCDataVersion MC_1_20_6 = new MCDataVersion(3839);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21
     */
    public static final MCDataVersion MC_1_21 = new MCDataVersion(3953);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.1
     */
    public static final MCDataVersion MC_1_21_1 = new MCDataVersion(3955);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.2
     */
    public static final MCDataVersion MC_1_21_2 = new MCDataVersion(4080);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.3
     */
    public static final MCDataVersion MC_1_21_3 = new MCDataVersion(4082);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.4
     */
    public static final MCDataVersion MC_1_21_4 = new MCDataVersion(4189);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.5
     */
    public static final MCDataVersion MC_1_21_5 = new MCDataVersion(4325);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.6
     */
    public static final MCDataVersion MC_1_21_6 = new MCDataVersion(4435);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.7
     */
    public static final MCDataVersion MC_1_21_7 = new MCDataVersion(4438);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.8
     */
    public static final MCDataVersion MC_1_21_8 = new MCDataVersion(4440);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.9
     */
    public static final MCDataVersion MC_1_21_9 = new MCDataVersion(4554);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.10
     */
    public static final MCDataVersion MC_1_21_10 = new MCDataVersion(4556);

    /**
     * A {@link MCDataVersion} that represents Minecraft 1.21.11
     */
    public static final MCDataVersion MC_1_21_11 = new MCDataVersion(4671);

    /**
     * Creates a {@link MCDataVersion} from the specified data version
     *
     * @param dataVersion the data version
     * @return a {@link MCDataVersion}
     */
    @Contract("_ -> new")
    public static @NotNull MCDataVersion of(int dataVersion) {
        return new MCDataVersion(dataVersion);
    }

    /**
     * Gets {@link MCDataVersion} of the server on which Box is currently running.
     *
     * @return {@link MCDataVersion} of the server on which Box is currently running
     */
    @SuppressWarnings("deprecation")
    @Contract("-> new")
    public static @NotNull MCDataVersion current() {
        return MCDataVersion.of(Bukkit.getUnsafe().getDataVersion());
    }

    @Override
    public int compareTo(@NotNull MCDataVersion other) {
        return Integer.compare(this.dataVersion, other.dataVersion);
    }

}
