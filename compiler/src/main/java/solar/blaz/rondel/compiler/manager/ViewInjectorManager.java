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

import android.app.Activity;
import android.view.View;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import solar.blaz.rondel.BaseComponent;
import solar.blaz.rondel.Mvp;
import solar.blaz.rondel.ViewScope;
import solar.blaz.rondel.compiler.model.ComponentModel;
import solar.blaz.rondel.compiler.model.InjectorModel;

import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.google.auto.common.MoreElements.getAnnotationMirror;

/**
 * Created by blazsolar on 05/03/16.
 */
@Singleton
public class ViewInjectorManager extends AbstractInjectorManager {

    private final Filer filer;
    private final Elements elementsUtil;
    private final Types typesUtil;

    @Inject
    protected ViewInjectorManager(Messager messager, Elements elementUtils, Filer filer, Elements elementsUtil, Types typesUtil) {
        super(messager, elementUtils, typesUtil);
        this.filer = filer;
        this.elementsUtil = elementsUtil;
        this.typesUtil = typesUtil;
    }

    public ComponentModel parse( Element element) {

        if (!isValidType(element)) {
            return null;
        }

        AnnotationMirror annotationMirror = getAnnotationMirror(element, Mvp.class).get();

        ImmutableList<TypeMirror> modules = convertClassArrayToListOfTypes(annotationMirror, "modules");
        TypeElement component = parseViewComponent(convertClassToType(annotationMirror, "component"));
        TypeElement[] modleElements = parseModuleElements(modules);

        TypeMirror parent = verifyParent(element, convertClassToType(annotationMirror, "parent"));

        if (component != null && modleElements != null) {
            InjectorModel injectorModel = new InjectorModel(element);
            injectorModel.name = "MVP" + element.getSimpleName();
            injectorModel.packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
            injectorModel.view = element.asType();
            injectorModel.modules = modleElements;
            injectorModel.component = component;
            injectorModel.superType = ((TypeElement) element).getSuperclass();

            ComponentModel componentModel = new ComponentModel(element);
            componentModel.name = "MVP" + component.getSimpleName().toString();
            componentModel.packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
            componentModel.view = element.asType();
            componentModel.modules = modleElements;
            componentModel.component = component;
            componentModel.parent = parent;
            componentModel.injector = injectorModel;

            return componentModel;
        } else {
            return null;
        }

    }

    public void write(ComponentModel model, ComponentModel parent, List<ComponentModel> children) throws IOException {

        ClassName componentName = ClassName.get(model.component);

        String[] moduleNames;
        TypeElement[] modules = model.modules;
        if (modules != null && modules.length > 0) {
            moduleNames = new String[modules.length];
            for (int i = 0; i < modules.length; i++) {
                moduleNames[i] = modules[i].getSimpleName().toString() + ".class";
            }
        } else {
            moduleNames = new String[0];
        }

        AnnotationSpec annotation = AnnotationSpec.builder(ClassName.get("dagger", "Subcomponent"))
                .addMember("modules", String.join(", ", moduleNames))
                .build();

        TypeSpec injector = TypeSpec.interfaceBuilder(model.name)
                .addAnnotation(annotation)
                .addAnnotation(ViewScope.class)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(BaseComponent.class))
                .addSuperinterface(componentName)
                .addType(getComponentBuilder(model))
                .addMethods(getChildMethodBuilders(children))
                .addMethod(MethodSpec.methodBuilder("inject")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(TypeName.get(model.view), "view")
                        .build())
                .build();

        JavaFile.builder(model.packageName, injector)
                .indent("    ")
                .build()
                .writeTo(filer);

        writeInjector(model.injector, parent);

    }

    private TypeSpec getComponentBuilder(ComponentModel model) {

        TypeElement module = model.modules[0];
        String moduleName = module.getSimpleName().toString();
        TypeName moduleType = TypeName.get(module.asType());
        String name = model.component.getSimpleName().toString();
        ClassName componentName = ClassName.bestGuess("MVP" + name);
        String methodName = Character.toLowerCase(moduleName.charAt(0)) + moduleName.substring(1);

        return TypeSpec.interfaceBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addMethod(MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.bestGuess("Builder"))
                        .addParameter(moduleType, "module")
                        .build())
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(componentName)
                        .build())
                .addAnnotation(ClassName.get("dagger", "Subcomponent", "Builder"))
                .build();

    }

    private void writeInjector(InjectorModel model, ComponentModel parent) throws IOException {

        TypeSpec injector = TypeSpec.classBuilder(model.name)
                .addMethod(injectMethod(model, parent))
                .build();

        JavaFile.builder(model.packageName, injector)
                .indent("    ")
                .build()
                .writeTo(filer);


    }

    private MethodSpec injectMethod(InjectorModel model, ComponentModel parent) {

        String name = model.component.getSimpleName().toString();
        String builderMethodName = Character.toLowerCase(name.charAt(0)) + name.substring(1) + "Builder";
        ClassName component =  ClassName.get(model.packageName, "MVP" + name);

        TypeElement typeElement = elementsUtil.getTypeElement(Activity.class.getCanonicalName());
        boolean isActivity = typesUtil.isSubtype(model.superType, typeElement.asType());

        ClassName appClass = (ClassName) ClassName.get(parent.element.asType());
        ClassName appComponentClass = ClassName.get(parent.packageName, "MVP" + parent.component.getSimpleName().toString());

        CodeBlock injectLogic;

        if (isActivity) {

            List<Object> formatParams = new ArrayList<Object>();
            formatParams.add(appClass);
            formatParams.add(appClass);
            formatParams.add(appComponentClass);
            formatParams.add(appComponentClass);
            formatParams.add(component);
            formatParams.add(builderMethodName);

            StringBuilder formatBuilder = new StringBuilder("$T app = ($T) injectie.getApplicationContext();\n" +
                    "$T baseComponent = ($T) app.getComponent();\n" +
                    "$T component = baseComponent.$L()\n");

            formatBuilder.append(formatBuilderModule(model.modules, formatParams, model.view));

            formatBuilder.append("        .build();\n" +
                    "component.inject(injectie);\n" +
                    "return component;");

            injectLogic = CodeBlock.builder()
                    .add(formatBuilder.toString(), formatParams.toArray())
                    .build();
        } else {

            List<Object> formatParams = new ArrayList<Object>();
            formatParams.add(appClass);
            formatParams.add(appClass);
            formatParams.add(appComponentClass);
            formatParams.add(appComponentClass);
            formatParams.add(component);
            formatParams.add(builderMethodName);

            StringBuilder formatBuilder = new StringBuilder("$T activity = ($T) injectie.getContext();\n" +
                    "$T baseComponent = ($T) activity.getComponent();\n" +
                    "$T component = baseComponent.$L()\n");

            formatBuilder.append(formatBuilderModule(model.modules, formatParams, model.view));

            formatBuilder.append("        .build();\n" +
                    "component.inject(injectie);\n" +
                    "return component;");

            injectLogic = CodeBlock.builder()
                    .add(formatBuilder.toString(), formatParams.toArray())
                    .build();

        }

        return MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(component)
                .addParameter(TypeName.get(model.view), "injectie")
                .addCode(injectLogic)
                .build();

    }

    private boolean isValidType(Element element) {

        TypeElement activityElement = elementsUtil.getTypeElement(Activity.class.getCanonicalName());
        TypeElement viewElement = elementsUtil.getTypeElement(View.class.getCanonicalName());
        return typesUtil.isSubtype(element.asType(), activityElement.asType()) ||
                typesUtil.isSubtype(element.asType(), viewElement.asType());

    }

}
