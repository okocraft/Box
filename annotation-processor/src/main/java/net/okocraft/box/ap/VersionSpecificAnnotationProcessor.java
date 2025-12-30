package net.okocraft.box.ap;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.ap.generator.VersionedImplGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({"net.okocraft.box.ap.annotation.version.VersionSpecific"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class VersionSpecificAnnotationProcessor extends AbstractProcessor {

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        List<String> classes = new ArrayList<>();

        for (Element element : roundEnv.getElementsAnnotatedWith(VersionSpecific.class)) {
            if (element instanceof TypeElement typeElement) {
                String implLoc = element.getEnclosingElement() + "." + element.getSimpleName() + "Impl";
                VersionedImplGenerator generator = new VersionedImplGenerator(typeElement);
                try {
                    JavaFileObject file = this.processingEnv.getFiler().createSourceFile(implLoc);

                    try (Writer writer = file.openWriter()) {
                        generator.writeTo(writer);
                    }
                } catch (Exception e) {
                    this.processingEnv.getMessager().printError(e.getMessage(), element);
                    continue;
                }
                classes.add(implLoc);
            }
        }

        if (classes.isEmpty()) {
            return false;
        }

        try {
            FileObject file = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/net.okocraft.box.version.common.version.Versioned");
            try (Writer writer = file.openWriter()) {
                for (int i = 0, classesSize = classes.size(); i < classesSize; i++) {
                    if (i != 0) writer.write('\n');
                    writer.write(classes.get(i));
                }
            }
        } catch (IOException e) {
            this.processingEnv.getMessager().printError(e.getMessage());
        }
        return true;
    }
}
