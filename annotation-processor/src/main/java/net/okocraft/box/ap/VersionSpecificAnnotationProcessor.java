package net.okocraft.box.ap;

import net.okocraft.box.ap.annotation.version.VersionSpecific;
import net.okocraft.box.ap.generator.VersionedImplGenerator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.util.ArrayList;
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
        var classes = new ArrayList<String>();

        for (var element : roundEnv.getElementsAnnotatedWith(VersionSpecific.class)) {
            if (element instanceof TypeElement typeElement) {
                var implLoc = element.getEnclosingElement() + "." + element.getSimpleName() + "Impl";
                var generator = new VersionedImplGenerator(typeElement);
                try {
                    var file = this.processingEnv.getFiler().createSourceFile(implLoc);

                    try (var writer = file.openWriter()) {
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
            var file = this.processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/net.okocraft.box.version.common.version.Versioned");
            try (var writer = file.openWriter()) {
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
