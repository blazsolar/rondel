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

package solar.blaz.rondel.compiler.manager;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import solar.blaz.rondel.App;
import solar.blaz.rondel.BaseAppComponent;
import solar.blaz.rondel.compiler.model.ComponentModel;
import solar.blaz.rondel.compiler.model.InjectorModel;

import javax.annotation.processing.Filer;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.google.auto.common.MoreElements.getAnnotationMirror;

/**
 * Created by blazsolar on 05/03/16.
 */
@Singleton
public class SingletonInjectorManager extends AbstractInjectorManager {

    private final Messager messager;
    private final Elements elementUtils;
    private final Filer filer;

    private ComponentModel appComponent;

    @Inject
    public SingletonInjectorManager(Messager messager, Elements elementUtils, Filer filer, Types types) {
        super(messager, elementUtils, types);
        this.messager = messager;
        this.elementUtils = elementUtils;
        this.filer = filer;
    }

    public ComponentModel parse(RoundEnvironment env) {
        if (appComponent == null) {
            Set<? extends Element> apps = env.getElementsAnnotatedWith(App.class);
            if (apps.size() == 1) {
                for (Element app : apps) {

                    Optional<AnnotationMirror> annotationMirrorOptional = getAnnotationMirror(app, App.class);
                    if (annotationMirrorOptional.isPresent()) {
                        AnnotationMirror annotationMirror = annotationMirrorOptional.get();
                        ImmutableList<TypeMirror> modules = convertClassArrayToListOfTypes(annotationMirror, "modules");

                        TypeElement componentElement = parseViewComponent(convertClassToType(annotationMirror, "component"));
                        TypeElement[] modleElements = parseModuleElements(modules);

                        if (componentElement != null && modleElements != null) {
                            ComponentModel componentModel = new ComponentModel(app);
                            componentModel.name = "MVP" + componentElement.getSimpleName().toString();
                            componentModel.packageName = elementUtils.getPackageOf(app).getQualifiedName().toString();
                            componentModel.view = app.asType();
                            componentModel.modules = modleElements;
                            componentModel.component = componentElement;

                            InjectorModel injectorModel = new InjectorModel(app);
                            injectorModel.name = "MVP" + app.getSimpleName();
                            injectorModel.packageName = elementUtils.getPackageOf(app).getQualifiedName().toString();
                            injectorModel.view = app.asType();

                            injectorModel.component = componentElement;
                            injectorModel.superType = ((TypeElement) app).getSuperclass();
                            injectorModel.modules = modleElements;
                            componentModel.injector = injectorModel;

                            appComponent = componentModel;
                            return appComponent;
                        }
                    }
                    break;
                }
            } else if (apps.size() == 0) {
                messager.error("No App level view provided.");
            } else {
                messager.error("Only one App level view is allowed.");
            }
        }

        return null;
    }

    public void write(List<ComponentModel> children) throws IOException {

        TypeSpec.Builder component = createComponent(appComponent);

        component.addMethod(MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                .addParameter(TypeName.get(appComponent.element.asType()), "app")
                .build());

        component.addMethods(getChildMethodBuilders(children));

        JavaFile.builder(appComponent.packageName, component.build())
                .indent("    ")
                .build()
                .writeTo(filer);

        writeAppInjector(appComponent.injector);

    }

    public boolean hasComponent() {
        return appComponent != null;
    }

    public ComponentModel getComponent() {
        return appComponent;
    }

    private TypeSpec.Builder createComponent(ComponentModel model) {

        ClassName componentName = ClassName.bestGuess(model.component.getSimpleName().toString());

        TypeElement[] modules = model.modules;

        CodeBlock.Builder codeBlock = CodeBlock.builder()
                .add("{ ");

        if (modules != null && modules.length > 0) {
            for (int i = 0; i < modules.length; i++) {
                if (i > 0) {
                    codeBlock.add(", ");
                }
                codeBlock.add("$T.class", modules[i].asType());
            }
        }

        codeBlock.add(" }");

        AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get("dagger", "Component"))
                .addMember("modules", codeBlock.build())
                .build();

        return TypeSpec.interfaceBuilder(model.name)
                .addAnnotation(annotation)
                .addAnnotation(ClassName.get("javax.inject", "Singleton"))
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(BaseAppComponent.class)
                .addSuperinterface(componentName);

    }

    private void writeAppInjector(InjectorModel model) throws IOException {

        String name = model.component.getSimpleName().toString();
        ClassName component =  ClassName.get(model.packageName, "MVP" + name);
        ClassName daggerComponent =  ClassName.get(model.packageName, "DaggerMVP" + name);

        List<Object> formatParams = new ArrayList<Object>();
        formatParams.add(component);
        formatParams.add(daggerComponent);

        String methodFormat = "$T component = $T.builder()\n";

        methodFormat += formatBuilderModule(model.modules, formatParams, model.view);

        methodFormat += "        .build();\n" +
                "component.inject(injectie);\n" +
                "return component;";

        CodeBlock injectLogic = CodeBlock.builder()
                .add(methodFormat, formatParams.toArray())
                .build();

        TypeSpec injector = TypeSpec.classBuilder(model.name)
                .addModifiers(Modifier.PUBLIC)
                .addMethod(MethodSpec.methodBuilder("inject")
                        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                        .returns(component)
                        .addParameter(TypeName.get(model.view), "injectie")
                        .addCode(injectLogic)
                        .build())
                .build();

        JavaFile.builder(model.packageName, injector)
                .indent("    ")
                .build()
                .writeTo(filer);


    }

}
