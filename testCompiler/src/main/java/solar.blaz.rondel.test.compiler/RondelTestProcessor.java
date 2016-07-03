/*
 *    Copyright 2016 Blaž Šolar
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package solar.blaz.rondel.test.compiler;

import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
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
public class RondelTestProcessor extends AbstractProcessor {

    @Override public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        System.out.println("Initing processor");
    }

    @Override public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        System.out.println("process call");

        Set<? extends Element> apps = roundEnv.getElementsAnnotatedWith(App.class);

        if (apps.size() == 1) {

            for (Element app : apps) {

                String packageName = processingEnv.getElementUtils().getPackageOf(app).getQualifiedName().toString();
                ClassName rondelApp = ClassName.get(packageName, "Rondel" + app.getSimpleName());
                ClassName appComponent = ClassName.get(packageName, "Rondel" + app.getSimpleName() + "Component");
                TypeSpec.Builder builder = TypeSpec.classBuilder("Test" + app.getSimpleName())
                        .superclass(TypeName.get(app.asType()))
                        .addModifiers(Modifier.PUBLIC)
                        .addField(FieldSpec.builder(appComponent, "component", Modifier.PRIVATE)
                                .build())
                        .addMethod(MethodSpec.methodBuilder("reInject")
                                .addCode("component = $T.inject(this);", rondelApp)
                                .addModifiers(Modifier.PUBLIC)
                                .build())
                        .addMethod(MethodSpec.methodBuilder("getComponent")
                                .addAnnotation(ClassName.get(Override.class))
                                .returns(appComponent)
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
            processingEnv.getMessager().printMessage(Kind.WARNING, "No App level view provided.");
        } else {
            processingEnv.getMessager().printMessage(Kind.WARNING, "Only one App level view is allowed.");
        }

        return false;
    }

    @Override public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(App.class.getName());
    }
}
