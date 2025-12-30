package net.okocraft.box.feature.gui.internal.util;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class XmasChecker {

    static final AtomicLong NEXT_XMAS_CHECK_TIME = new AtomicLong();
    static final AtomicBoolean IS_XMAS = new AtomicBoolean(false);

    public static boolean isXmas() {
        checkXmas();
        return IS_XMAS.get();
    }

    private static void checkXmas() {
        if (System.currentTimeMillis() < NEXT_XMAS_CHECK_TIME.get()) {
            return;
        }

        LocalDateTime today = LocalDateTime.now();

        IS_XMAS.set(
            today.getMonth() == Month.DECEMBER &&
            (today.getDayOfMonth() == 24 || today.getDayOfMonth() == 25)
        );

        NEXT_XMAS_CHECK_TIME.set(
            today.truncatedTo(ChronoUnit.DAYS)
                .plusDays(1)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        );
    }
}
