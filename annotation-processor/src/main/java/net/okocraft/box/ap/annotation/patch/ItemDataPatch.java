package net.okocraft.box.ap.annotation.patch;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ItemDataPatch {

    private ItemDataPatch() {
        throw new UnsupportedOperationException();
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    public @interface UpdateItemData {
        String[] targets();
    }

}
