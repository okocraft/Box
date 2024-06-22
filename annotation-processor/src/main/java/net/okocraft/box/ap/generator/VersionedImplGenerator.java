package net.okocraft.box.ap.generator;

import net.okocraft.box.ap.annotation.version.VersionSpecific;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.List;

public final class VersionedImplGenerator {

    private static final char NEWLINE = '\n';

    private static final List<String> IMPORTS = List.of(
            "net.okocraft.box.api.util.MCDataVersion",
            "net.okocraft.box.storage.api.model.item.provider.DefaultItem",
            "net.okocraft.box.version.common.version.Versioned",
            "java.util.stream.Stream"
    );

    private final TypeElement element;

    public VersionedImplGenerator(TypeElement element) {
        this.element = element;
    }

    public void writeTo(Writer writer) throws IOException {
        writer.append("package ").append(this.element.getEnclosingElement().toString()).append(";").append(NEWLINE);
        writer.write(NEWLINE);

        for (var i : IMPORTS) {
            writer.append("import ").append(i).append(";").append(NEWLINE);
        }
        writer.write(NEWLINE);

        writer.append("public final class ").append(this.element.getSimpleName()).append("Impl implements Versioned {").append(NEWLINE);
        writer.write(NEWLINE);

        this.writeClassBody(writer);

        writer.append('}').append(NEWLINE);
    }

    private void writeClassBody(Writer writer) throws IOException {
        this.addDefaultItemVersion(writer);
        writer.write(NEWLINE);

        this.addDefaultItems(writer);
        writer.write(NEWLINE);
    }

    private void addDefaultItemVersion(Writer writer) throws IOException {
        var verField =
                ElementFilter.fieldsIn(this.element.getEnclosedElements())
                        .stream()
                        .filter(v -> v.getAnnotation(VersionSpecific.Version.class) != null)
                        .findFirst()
                        .map(v -> this.element.getSimpleName() + "." + v.getSimpleName())
                        .orElseThrow(() -> new IllegalStateException("@VersionSpecific.Version with a MCDataVersion field not found"));

        writer.append("    public MCDataVersion version() {").append(NEWLINE);
        writer.append("        return ").append(verField).append(";").append(NEWLINE);
        writer.append("    }").append(NEWLINE);
    }

    private void addDefaultItems(Writer writer) throws IOException {
        var method = ElementFilter.methodsIn(this.element.getEnclosedElements())
                .stream()
                .filter(e -> e.getAnnotation(VersionSpecific.DefaultItemSource.class) != null)
                .map(e -> this.element.getSimpleName() + "." + e.getSimpleName() + "()")
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("@DefaultItemSource not found"));

        writer.append("    public Stream<DefaultItem> defaultItems() {\n");
        writer.append("        return ").append(method).append(";\n");
        writer.append("    }\n");
    }
}
