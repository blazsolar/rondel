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
import android.app.Application;
import android.app.Service;
import android.view.View;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import solar.blaz.rondel.BaseComponent;
import solar.blaz.rondel.Rondel;
import solar.blaz.rondel.ViewScope;
import solar.blaz.rondel.compiler.Constants;
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
    private final Messager messager;

    @Inject
    protected ViewInjectorManager(Messager messager, Elements elementUtils, Filer filer, Elements elementsUtil, Types typesUtil) {
        super(messager, elementUtils, typesUtil);
        this.messager = messager;
        this.filer = filer;
        this.elementsUtil = elementsUtil;
        this.typesUtil = typesUtil;
    }

    public ComponentModel parse(Element element) {

        if (!isValidType(element)) {
            return null;
        }

        AnnotationMirror annotationMirror = getAnnotationMirror(element, Rondel.class).get();

        TypeElement[] components = parseViewComponent(convertClassArrayToListOfTypes(annotationMirror, "components"));
        TypeElement[] moduleElements = parseModuleElements(convertClassArrayToListOfTypes(annotationMirror, "modules"));

        TypeMirror parent = verifyParent(element, convertClassToType(annotationMirror, "parent"));

        InjectorModel injectorModel = new InjectorModel(element);
        injectorModel.name = Constants.CLASS_PREFIX + element.getSimpleName();
        injectorModel.packageName = elementsUtil.getPackageOf(element).getQualifiedName().toString();
        injectorModel.view = element.asType();
        injectorModel.modules = moduleElements;
        injectorModel.superType = ((TypeElement) element).getSuperclass();

        ComponentModel componentModel = new ComponentModel(element);
        componentModel.name = Constants.CLASS_PREFIX + element.getSimpleName() + "Component";
        componentModel.packageName = ((PackageElement) element.getEnclosingElement()).getQualifiedName().toString();
        componentModel.view = element.asType();
        componentModel.modules = moduleElements;
        componentModel.components = components;
        componentModel.parent = parent;
        componentModel.injector = injectorModel;
        injectorModel.component = componentModel;

        return componentModel;

    }

    public void write(ComponentModel model, ComponentModel parent, List<ComponentModel> children) throws IOException {

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

        AnnotationSpec.Builder subcomponentAnnotation =
                AnnotationSpec.builder(ClassName.get("dagger", "Subcomponent"));

        if (moduleNames.length > 0) {
            subcomponentAnnotation.addMember("modules", "{ " + String.join(", ", moduleNames) + " }");
        }

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(model.name)
                .addAnnotation(subcomponentAnnotation.build())
                .addAnnotation(ViewScope.class)
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(BaseComponent.class))
                .addType(getComponentBuilder(model))
                .addMethods(getChildMethodBuilders(children))
                .addMethod(MethodSpec.methodBuilder("inject")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(TypeName.get(model.view), "view")
                        .build());

        if (model.components != null && model.components.length > 0) {
            for (TypeElement component : model.components) {
                ClassName componentName = ClassName.get(component);
                builder.addSuperinterface(componentName);
            }
        }

        JavaFile.builder(model.packageName, builder.build())
                .indent("    ")
                .build()
                .writeTo(filer);

        writeInjector(model.injector, parent);

    }

    private TypeSpec getComponentBuilder(ComponentModel model) {

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder("Builder")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC);

        TypeElement[] modules = model.modules;
        if (modules != null && modules.length > 0) {
            for (TypeElement module : modules) {

                String moduleName = module.getSimpleName().toString();
                TypeName moduleType = TypeName.get(module.asType());
                String methodName = Character.toLowerCase(moduleName.charAt(0)) + moduleName.substring(1);

                builder.addMethod(MethodSpec.methodBuilder(methodName)
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.bestGuess("Builder"))
                        .addParameter(moduleType, "module")
                        .build());
            }
        }

        String name = model.name;
        ClassName componentName = ClassName.bestGuess(name);

        return builder
                .addMethod(MethodSpec.methodBuilder("build")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(componentName)
                        .build())
                .addAnnotation(ClassName.get("dagger", "Subcomponent", "Builder"))
                .build();

    }

    private void writeInjector(InjectorModel model, ComponentModel parent) throws IOException {

        TypeSpec.Builder injector = TypeSpec.classBuilder(model.name);

        addInjectMethods(model, parent, injector);
        addTestSpecs(model.modules, injector, model.view);

        JavaFile.builder(model.packageName, injector.build())
                .indent("    ")
                .build()
                .writeTo(filer);

    }

    private void addInjectMethods(InjectorModel model, ComponentModel parent, TypeSpec.Builder injector) {

        String name = model.component.name;
        String builderMethodName = Character.toLowerCase(name.charAt(0)) + name.substring(1) + "Builder";
        ClassName component =  ClassName.get(model.packageName, name);

        TypeElement typeElement = elementsUtil.getTypeElement(Activity.class.getCanonicalName());
        boolean isActivity = typesUtil.isSubtype(model.superType, typeElement.asType());
        TypeElement serviceElement = elementsUtil.getTypeElement(Service.class.getCanonicalName());
        boolean isService = typesUtil.isSubtype(model.superType, serviceElement.asType());
        TypeElement viewElement = elementsUtil.getTypeElement(View.class.getCanonicalName());
        boolean isView = typesUtil.isSubtype(model.superType, viewElement.asType());

        ClassName appClass = (ClassName) ClassName.get(parent.element.asType());
        ClassName appComponentClass = ClassName.get(parent.packageName, parent.name);

        CodeBlock injectLogic;

        if (isActivity || isService) {

            injector.addField(appComponentClass, "component", Modifier.PRIVATE, Modifier.STATIC);

            injector.addMethod(MethodSpec.methodBuilder("getComponent")
                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                    .returns(appComponentClass)
                    .addParameter(appClass, "app")
                    .addCode(CodeBlock.builder()
                            .add("if (component != null) {")
                            .add("return component;")
                            .add("} else {")
                            .add("return ($T) app.getComponent();", appComponentClass)
                            .add("}")
                            .build())
                    .build());

            injector.addMethod(MethodSpec.methodBuilder("setComponent")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .addParameter(appComponentClass, "component")
                    .addCode("$L.component = component;", model.name)
                    .build());

            List<Object> formatParams = new ArrayList<>();
            formatParams.add(appClass);
            formatParams.add(appClass);
            formatParams.add(component);
            formatParams.add(builderMethodName);

            StringBuilder formatBuilder = new StringBuilder("$T app = ($T) injectie.getApplicationContext();\n" +
                    "$T component = getComponent(app).$L()\n");

            formatBuilder.append(formatBuilderModule(model.modules, formatParams));

            formatBuilder.append("        .build();\n" +
                    "component.inject(injectie);\n" +
                    "return component;");

            injectLogic = CodeBlock.builder()
                    .add(formatBuilder.toString(), formatParams.toArray())
                    .build();

        } else if (isView) {

            List<Object> formatParams = new ArrayList<>();
            formatParams.add(appClass);
            formatParams.add(appClass);
            formatParams.add(appComponentClass);
            formatParams.add(appComponentClass);
            formatParams.add(component);
            formatParams.add(builderMethodName);

            TypeElement appElement = elementsUtil.getTypeElement(Application.class.getCanonicalName());
            boolean isApp = typesUtil.isSubtype(parent.element.asType(), appElement.asType());

            StringBuilder formatBuilder = new StringBuilder();
            if (isApp) {
                formatBuilder.append("$T parent = ($T) injectie.getContext().getApplicationContext();\n");
            } else {
                formatBuilder.append("$T parent = ($T) injectie.getContext();\n");
            }

            formatBuilder.append("$T baseComponent = ($T) parent.getComponent();\n" +
                    "$T component = baseComponent.$L()\n");

            formatBuilder.append(formatBuilderModule(model.modules, formatParams));

            formatBuilder.append("        .build();\n" +
                    "component.inject(injectie);\n" +
                    "return component;");

            injectLogic = CodeBlock.builder()
                    .add(formatBuilder.toString(), formatParams.toArray())
                    .build();

        } else {
            messager.error("Injected view not valid type.", model.element);
            injectLogic = null;
        }

        if (injectLogic != null) {
            injector.addMethod(MethodSpec.methodBuilder("inject")
                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                    .returns(component)
                    .addParameter(TypeName.get(model.view), "injectie")
                    .addCode(injectLogic)
                    .build());
        }

    }

    private boolean isValidType(Element element) {

        TypeElement activityElement = elementsUtil.getTypeElement(Activity.class.getCanonicalName());
        TypeElement serviceElement = elementsUtil.getTypeElement(Service.class.getCanonicalName());
        TypeElement viewElement = elementsUtil.getTypeElement(View.class.getCanonicalName());
        return typesUtil.isSubtype(element.asType(), activityElement.asType()) ||
                typesUtil.isSubtype(element.asType(), serviceElement.asType()) ||
                typesUtil.isSubtype(element.asType(), viewElement.asType());

    }

}
