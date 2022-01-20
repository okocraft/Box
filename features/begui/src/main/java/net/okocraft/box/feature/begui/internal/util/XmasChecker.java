package net.okocraft.box.feature.begui.internal.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
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

        var today = LocalDate.now();

        IS_XMAS.set(
                today.getMonth() == Month.DECEMBER &&
                        (today.getDayOfMonth() == 24 || today.getDayOfMonth() == 25)
        );

        if (NEXT_XMAS_CHECK_TIME.get() == 0) {
            NEXT_XMAS_CHECK_TIME.set(
                    Instant.now().plus(1, ChronoUnit.HOURS)
                            .truncatedTo(ChronoUnit.HOURS).toEpochMilli()
            );
        } else {
            NEXT_XMAS_CHECK_TIME.addAndGet(TimeUnit.HOURS.toMillis(1));
        }
    }
}
