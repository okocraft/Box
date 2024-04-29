package net.okocraft.box.ap.annotation.patch;

import net.okocraft.box.ap.annotation.holder.RenameHolder;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

public final class ItemNamePatch {

    private ItemNamePatch() {
        throw new UnsupportedOperationException();
    }

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.CLASS)
    @Repeatable(RenameHolder.class)
    public @interface Rename {

        String oldName();

        String newName();

    }
}
