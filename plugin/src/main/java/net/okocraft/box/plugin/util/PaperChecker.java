package net.okocraft.box.plugin.util;

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

    public static boolean isPaper() {
        return PAPER;
    }
}
