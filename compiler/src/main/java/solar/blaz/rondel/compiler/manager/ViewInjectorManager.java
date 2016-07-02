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

import android.view.ViewParent;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import solar.blaz.rondel.ActivityScope;
import solar.blaz.rondel.BaseComponent;
import solar.blaz.rondel.FragmentScope;
import solar.blaz.rondel.Rondel;
import solar.blaz.rondel.ServiceScope;
import solar.blaz.rondel.ViewScope;
import solar.blaz.rondel.compiler.Constants;
import solar.blaz.rondel.compiler.model.ComponentModel;
import solar.blaz.rondel.compiler.model.InjectorModel;

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
        TypeElement scope = verifyScope(convertClassToType(annotationMirror, "scope"));

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
        componentModel.scope = scope;
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
                .addAnnotation(getGeneratedAnnotation())
                .addAnnotation(subcomponentAnnotation.build())
                .addModifiers(Modifier.PUBLIC)
                .addSuperinterface(ClassName.get(BaseComponent.class))
                .addType(getComponentBuilder(model))
                .addMethods(getChildMethodBuilders(children))
                .addMethod(MethodSpec.methodBuilder("inject")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .addParameter(TypeName.get(model.view), "view")
                        .build());

        if (model.scope == null) {

            TypeMirror elementType = model.element.asType();
            boolean isActivity = isActivity(elementType);
            boolean isService = isService(elementType);
            boolean isFragment = isFragment(elementType);
            boolean isView = isView(elementType);

            if (isActivity) {
                builder.addAnnotation(ActivityScope.class);
            } else if (isService) {
                builder.addAnnotation(ServiceScope.class);
            } else if (isFragment) {
                builder.addAnnotation(FragmentScope.class);
            } else if (isView) {
                builder.addAnnotation(ViewScope.class);
            } else {
                messager.error("Scope for type could not be found");
            }

        } else {
            builder.addAnnotation(ClassName.get(model.scope));
        }

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

        TypeSpec.Builder injector = TypeSpec.classBuilder(model.name)
                .addAnnotation(getGeneratedAnnotation());

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

        boolean isActivity = isActivity(model.superType);
        boolean isService = isService(model.superType);
        boolean isFragment = isFragment(model.superType);
        boolean isView = isView(model.superType);

        ClassName parentClass = (ClassName) ClassName.get(parent.element.asType());
        ClassName parentComponentClass = ClassName.get(parent.packageName, parent.name);

        CodeBlock injectLogic;
        StringBuilder formatBuilder = new StringBuilder();
        List<Object> formatParams = new ArrayList<>();

        if (isActivity || isService) {

            formatParams.add(parentClass);
            formatParams.add(parentClass);

            formatBuilder.append("$T parent = ($T) injectie.getApplicationContext();\n");

        } else if (isFragment) {

            formatParams.add(parentClass);
            formatParams.add(parentClass);

            boolean isParentApp = isApplication(parent.element.asType());
            boolean isParentActivity = isActivity(parent.element.asType());
            boolean isParentFragment = isFragment(parent.element.asType());

            if (isParentApp) {
                formatBuilder.append("$T parent = ($T) injectie.getActivity().getApplicationContext();\n");
            } else if (isParentActivity) {
                formatBuilder.append("$T parent = ($T) injectie.getActivity();\n");
            } else if (isParentFragment) {
                formatBuilder.append("$T parent = ($T) injectie.getParentFragment();\n");
            } else {
                messager.error("Unknown parent type", model.element);
                return;
            }

        } else if (isView) {

            formatParams.add(parentClass);
            formatParams.add(parentClass);

            boolean isParentApp = isApplication(parent.element.asType());
            boolean isParentActivity = isActivity(parent.element.asType());
            boolean isParentView = isView(parent.element.asType());

            formatBuilder = new StringBuilder();
            if (isParentApp) {
                formatBuilder.append("$T parent = ($T) injectie.getContext().getApplicationContext();\n");
            } else if (isParentActivity) {
                formatBuilder.append("$T parent = ($T) injectie.getContext();\n");
            } else if (isParentView) {
                formatBuilder.append("$T parent = ($T) getParent(injectie.getParent());\n");
                injector.addMethod(MethodSpec.methodBuilder("getParent")
                        .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                        .addParameter(ViewParent.class, "view")
                        .returns(parentClass)
                        .addCode(CodeBlock.of("if (view instanceof $T) {\n"
                                + "    return ($T) view;\n"
                                + "} else {\n"
                                + "    ViewParent parent = view.getParent();\n"
                                + "    if (parent == null) {\n"
                                + "        throw new IllegalStateException(\"Parent not found\");\n"
                                + "    } else {\n"
                                + "        return getParent(parent);\n"
                                + "    }\n"
                                + "}\n", parentClass, parentClass))
                        .build());
            } else {
                messager.error("Unknown parent type", model.element);
                return;
            }

        } else {
            messager.error("Injected view not valid type.", model.element);
            return;
        }

        formatParams.add(parentComponentClass);
        formatParams.add(parentComponentClass);
        formatParams.add(component);
        formatParams.add(builderMethodName);

        formatBuilder.append("$T baseComponent = ($T) parent.getComponent();\n" +
                "$T component = baseComponent.$L()\n");

        formatBuilder.append(formatBuilderModule(model.modules, formatParams));

        formatBuilder.append("        .build();\n" +
                "component.inject(injectie);\n" +
                "return component;");

        injectLogic = CodeBlock.builder()
                .add(formatBuilder.toString(), formatParams.toArray())
                .build();

        injector.addMethod(MethodSpec.methodBuilder("inject")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(component)
                .addParameter(TypeName.get(model.view), "injectie")
                .addCode(injectLogic)
                .build());

    }

    private boolean isValidType(Element element) {
        TypeMirror typeMirror = element.asType();
        return isActivity(typeMirror) || isService(typeMirror) || isFragment(typeMirror) || isView(typeMirror);
    }

}
