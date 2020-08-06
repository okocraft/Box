package net.okocraft.box.plugin.util;

/**
 * 実行しているサーバーソフトウェアが Paper もしくはそのフォークであることを確かめるクラス。
 */
public final class PaperChecker {

    private final static boolean PAPER = checkPaper();

    private static boolean checkPaper() {
        try {
            Class.forName("co.aikar.timings.Timing");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 実行しているサーバーソフトウェアが Paper もしくはそのフォークであるか。
     *
     * @return サーバーソフトウェアが Paper なら {@code true}, そうでなければ {@code false}
     */
    public static boolean isPaper() {
        return PAPER;
    }
}
