package net.okocraft.box.ap.generator;

import net.okocraft.box.ap.annotation.holder.RenameHolder;
import net.okocraft.box.ap.annotation.patch.ItemDataPatch;
import net.okocraft.box.ap.annotation.patch.ItemNamePatch;
import net.okocraft.box.ap.annotation.source.DefaultItemSource;
import net.okocraft.box.ap.annotation.version.DefaultItemVersion;

import javax.lang.model.element.TypeElement;
import javax.lang.model.util.ElementFilter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VersionedImplGenerator {

    private static final char NEWLINE = '\n';

    private static final List<String> IMPORTS = List.of(
            "net.okocraft.box.api.model.item.ItemVersion",
            "net.okocraft.box.api.util.MCDataVersion",
            "net.okocraft.box.storage.api.util.item.DefaultItem",
            "net.okocraft.box.storage.api.util.item.patcher.ItemDataPatcher",
            "net.okocraft.box.storage.api.util.item.patcher.ItemNamePatcher",
            "net.okocraft.box.version.common.version.Versioned",
            "java.util.Set",
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

        this.addPatchers(writer, "Stream<ItemNamePatcher> itemNamePatchers()", this.findItemNamePatcher());
        writer.write(NEWLINE);

        var fields = new ArrayList<String>();
        var dataPatchers = this.findItemDataPatcher(fields);

        if (!fields.isEmpty()) {
            for (var field : fields) {
                writer.append("    ").append(field).append('\n');
            }
            writer.write(NEWLINE);
        }

        this.addPatchers(writer, "Stream<ItemDataPatcher> itemDataPatchers()", dataPatchers);
        writer.write(NEWLINE);
    }

    private void addDefaultItemVersion(Writer writer) throws IOException {
        var verField =
                ElementFilter.fieldsIn(this.element.getEnclosedElements())
                        .stream()
                        .filter(v -> v.getAnnotation(DefaultItemVersion.class) != null)
                        .findFirst()
                        .map(v -> this.element.getSimpleName() + "." + v.getSimpleName())
                        .orElseThrow(() -> new IllegalStateException("@DefaultItemVersion with a ItemVersion field not found"));

        writer.append("    public ItemVersion defaultItemVersion() {").append(NEWLINE);
        writer.append("        return ").append(verField).append(";").append(NEWLINE);
        writer.append("    }").append(NEWLINE);
    }

    private void addDefaultItems(Writer writer) throws IOException {
        var method = ElementFilter.methodsIn(this.element.getEnclosedElements())
                .stream()
                .filter(e -> e.getAnnotation(DefaultItemSource.class) != null)
                .map(e -> this.element.getSimpleName() + "." + e.getSimpleName() + "()")
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("@DefaultItemSource not found"));

        writer.append("    public Stream<DefaultItem> defaultItems() {\n");
        writer.append("        return ").append(method).append(";\n");
        writer.append("    }\n");
    }

    private void addPatchers(Writer writer, String method, List<String> patchers) throws IOException {
        writer.append("    public ").append(method).append(" {\n");
        writer.append("        return Stream.of(\n");
        for (int i = 0, patchersSize = patchers.size(); i < patchersSize; i++) {
            writer.append("                ").append(patchers.get(i));
            if (i + 1 != patchersSize) {
                writer.append(",");
            }
            writer.write('\n');
        }
        writer.write("        );\n");
        writer.append("    }\n");
    }

    private List<String> findItemNamePatcher() {
        var result = new ArrayList<String>();

        this.element.getEnclosedElements()
                .stream()
                .flatMap(e -> {
                    var singleAnnotation = e.getAnnotation(ItemNamePatch.Rename.class);
                    if (singleAnnotation != null) {
                        return Stream.of("name -> name.equals(\"" + singleAnnotation.oldName() + "\") ? \"" + singleAnnotation.newName() + "\" : name");
                    }

                    var holderAnnotation = e.getAnnotation(RenameHolder.class);

                    if (holderAnnotation != null) {
                        return Arrays.stream(holderAnnotation.value()).map(annotation -> "name -> name.equals(\"" + annotation.oldName() + "\") ? \"" + annotation.newName() + "\" : name");
                    }

                    return Stream.empty();
                })
                .filter(Objects::nonNull)
                .forEach(result::add);

        return result;
    }

    private List<String> findItemDataPatcher(List<String> fieldsCollector) {
        var result = new ArrayList<String >();
        var counter = new AtomicInteger();

        this.element.getEnclosedElements()
                .forEach(e -> {
                    var annotation = e.getAnnotation(ItemDataPatch.UpdateItemData.class);
                    if (annotation != null) {
                        var filter = switch (annotation.targets().length) {
                            case 0 -> null;
                            case 1 -> "data.plainName().equals(\"" + annotation.targets()[0] + "\")";
                            default -> {
                                var fieldName = "ITEM_DATA_PATCH_TARGETS_" + counter.incrementAndGet();
                                fieldsCollector.add("private static final Set<String> " + fieldName + " = Set.of(" + Arrays.stream(annotation.targets()).map(target -> "\"" + target + "\"").collect(Collectors.joining(", ")) + ");");
                                yield fieldName + ".contains(data.plainName())";
                            }
                        };
                        result.add("data -> " + filter + " ? " + e.getEnclosingElement().getSimpleName() + "." + e.getSimpleName() + "(data)" + " : data");
                    }
                });

        return result;
    }
}
