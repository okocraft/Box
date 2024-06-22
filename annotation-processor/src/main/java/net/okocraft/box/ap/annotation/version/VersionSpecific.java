package net.okocraft.box.ap.annotation.version;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface VersionSpecific {

    @Target({ElementType.FIELD})
    @Retention(RetentionPolicy.CLASS)
    @interface Version {
    }

    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.CLASS)
    @interface DefaultItemSource {
    }
}
