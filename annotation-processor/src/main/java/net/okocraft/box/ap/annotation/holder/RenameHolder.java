package net.okocraft.box.ap.annotation.holder;

import net.okocraft.box.ap.annotation.patch.ItemNamePatch;
import org.jetbrains.annotations.ApiStatus;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.CLASS)
@ApiStatus.Internal
public @interface RenameHolder {

    ItemNamePatch.Rename[] value();

}
