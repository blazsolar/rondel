package solar.blaz.rondel.mock.compiler;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic.Kind;

import solar.blaz.rondel.App;

/**
 * Created by blaz on 09/06/16.
 */
public class RondelMockProcessor extends AbstractProcessor {

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        Set<? extends Element> apps = roundEnv.getElementsAnnotatedWith(App.class);

        if (apps.size() == 1) {

            for (Element app : apps) {

                String packageName = processingEnv.getElementUtils().getPackageOf(app).getQualifiedName().toString();
                ClassName appComponent = ClassName.get(packageName, "Rondel" + app.getSimpleName() + "Component");
                TypeSpec.Builder builder = TypeSpec.classBuilder("Mock" + app.getSimpleName())
                        .superclass(TypeName.get(app.asType()))
                        .addField(FieldSpec.builder(appComponent, "component", Modifier.PRIVATE)
                                .build())
                        .addMethod(MethodSpec.methodBuilder("reInject")
                                .addCode("component = $T.inject(this);", appComponent)
                                .build())
                        .addMethod(MethodSpec.methodBuilder("getComponent")
                                .addAnnotation(ClassName.get(Override.class))
                                .addModifiers(Modifier.PUBLIC)
                                .addCode(CodeBlock.builder()
                                        .add("if (component != null) {\n")
                                        .add("  return component;\n")
                                        .add("} else {\n")
                                        .add("  return super.getComponent();\n")
                                        .add("}")
                                        .build())
                                .build());


                try {
                    JavaFile.builder(packageName, builder.build())
                            .indent("    ")
                            .build()
                            .writeTo(processingEnv.getFiler());
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        } else if (apps.size() == 0) {
            processingEnv.getMessager().printMessage(Kind.ERROR, "No App level view provided.");
        } else {
            processingEnv.getMessager().printMessage(Kind.ERROR, "Only one App level view is allowed.");
        }

        return true;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(App.class.getName());
    }
}
