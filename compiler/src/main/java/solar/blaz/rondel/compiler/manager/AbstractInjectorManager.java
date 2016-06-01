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

import android.view.View;
import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.*;
import dagger.Module;
import solar.blaz.rondel.ComponentProvider;
import solar.blaz.rondel.compiler.model.ComponentModel;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.google.auto.common.AnnotationMirrors.getAnnotationValue;

/**
 * Created by blazsolar on 05/03/16.
 */
public abstract class AbstractInjectorManager {

    private final Messager messager;
    private final Elements elementUtils;
    private final Types typesUtil;

    protected AbstractInjectorManager(Messager messager, Elements elementUtils, Types typesUtil) {
        this.messager = messager;
        this.elementUtils = elementUtils;
        this.typesUtil = typesUtil;
    }

    protected TypeElement[] parseViewComponent(ImmutableList<TypeMirror> components) {

        if (components == null || components.size() == 0) {
            return null;
        } else {

            List<TypeElement> moduleElements = new ArrayList<>();
            for (int i = 0; i < components.size(); i++) {
                TypeMirror componentClass = components.get(i);

                TypeElement component = elementUtils.getTypeElement(componentClass.toString());

                if (component.getKind() == ElementKind.INTERFACE) {
                    moduleElements.add(component);
                } else {
                    messager.error("Component has to be interface.", component);
                }

            }

            if (moduleElements.isEmpty()) {
                return null;
            } else {
                return moduleElements.toArray(new TypeElement[moduleElements.size()]);
            }

        }

    }

    protected TypeMirror verifyParent(Element element, TypeMirror componentClass) {

        TypeElement voidElement = elementUtils.getTypeElement(Void.class.getCanonicalName());

        TypeElement viewProviderElement = elementUtils.getTypeElement(View.class.getCanonicalName());
        boolean isView = typesUtil.isSubtype(element.asType(), viewProviderElement.asType());

        if (componentClass == null || typesUtil.isSubtype(componentClass, voidElement.asType())) {
            return null; // no parent
        } else {

            // verify that is is provider
            TypeElement componentProviderElement = elementUtils.getTypeElement(ComponentProvider.class.getCanonicalName());

            if (isView) {
                if (typesUtil.isSubtype(componentClass, componentProviderElement.asType())) {
                    return componentClass;
                } else {
                    messager.error("Parent does not provide component.", element);
                }
            } else {
                messager.error("Only View can specify parent.", element);
            }

        }

        return null;

    }

    protected TypeElement[] parseModuleElements(ImmutableList<TypeMirror> modules) {
        if (modules == null || modules.size() == 0) {
            return null;
        } else {
            boolean validModules = true;

            TypeElement[] moduleElements = new TypeElement[modules.size()];
            for (int i = 0; i < modules.size(); i++) {
                TypeMirror moduleClass = modules.get(i);

                TypeElement module = elementUtils.getTypeElement(moduleClass.toString());

                if (module.getAnnotation(Module.class) == null) {
                    messager.error("App module is missing @Module annotation.");
                    validModules = false;
                } else {
                    moduleElements[i] = module;
                }
            }

            if (validModules) {
                return moduleElements;
            } else {
                return null;
            }
        }

    }

    protected ExecutableElement getConstructor(TypeElement module, TypeMirror injectedInstance) {

        List<? extends Element> enclosedElements = module.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.CONSTRUCTOR) {
                ExecutableElement constructor = (ExecutableElement) enclosedElement;
                List<? extends VariableElement> parameters = constructor.getParameters();

                if (parameters.size() == 0) {
                    return constructor;
                } else if (parameters.size() == 1 && typesUtil.isSubtype(injectedInstance, parameters.get(0).asType())) {
                    return constructor;
                }


            }
        }

        return null;
    }

    protected void addTestSpecs(TypeElement[] moduleElements, TypeSpec.Builder injector, TypeMirror injectedInstance) {

        if (moduleElements != null && moduleElements.length > 0) {
            for (TypeElement module : moduleElements) {
                TypeName moduleName = TypeName.get(module.asType());
                String moduleNameStringUpper = module.getSimpleName().toString();
                String moduleNameStringLower = moduleNameStringUpper.substring(0, 1).toLowerCase()
                        + moduleNameStringUpper.substring(1);

                ExecutableElement modelConstructor = getConstructor(module, injectedInstance);

                if (modelConstructor != null) {

                    CodeBlock.Builder modelMethod = CodeBlock.builder()
                            .add("if ($L != null) {", moduleNameStringLower)
                            .add("return $L;", moduleNameStringLower)
                            .add("} else {");


                    int paramCnt = modelConstructor.getParameters().size();
                    if (paramCnt == 1) {
                        modelMethod.add("return new $T(injectie);", moduleName);
                    } else if (paramCnt == 0) {
                        modelMethod.add("return new $T();", moduleName);
                    } else {
                        messager.error("Could not find constructor parameters.");
                    }

                    modelMethod.add("}");

                    injector
                            .addField(FieldSpec.builder(moduleName, moduleNameStringLower)
                                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                    .build())
                            .addMethod(MethodSpec.methodBuilder("set" + moduleNameStringUpper)
                                    .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                                    .addParameter(moduleName, "module")
                                    .addCode("$L = module;", moduleNameStringLower)
                                    .build())
                            .addMethod(MethodSpec.methodBuilder("get" + moduleNameStringUpper)
                                    .addModifiers(Modifier.PRIVATE, Modifier.STATIC)
                                    .addParameter(TypeName.get(injectedInstance), "injectie")
                                    .returns(moduleName)
                                    .addCode(modelMethod.build())
                                    .build())
                            .build();
                } else {
                    messager.error("No valid constructor for module.");
                }
            }
        }

    }

    protected String formatBuilderModule(TypeElement[] moduleElements, List<Object> formatParams) {

        StringBuilder builder = new StringBuilder();

        if (moduleElements != null && moduleElements.length > 0) {
            for (TypeElement module : moduleElements) {
                String moduleMethodName = module.getSimpleName().toString();
                String moduleMethodNameLower = Character.toLowerCase(moduleMethodName.charAt(0)) + moduleMethodName.substring(1);

                builder.append("        .$L(get$L(injectie))\n");
                formatParams.add(moduleMethodNameLower);
                formatParams.add(moduleMethodName);
            }
        }

        return builder.toString();
    }

    /**
     * Extracts the list of types that is the value of the annotation member {@code elementName} of
     * {@code annotationMirror}.
     *
     * @throws IllegalArgumentException if no such member exists on {@code annotationMirror}, or it
     *     exists but is not an array
     * @throws TypeNotPresentException if any of the values cannot be converted to a type
     */
    protected ImmutableList<TypeMirror> convertClassArrayToListOfTypes(
            AnnotationMirror annotationMirror, String elementName) {
        return TO_LIST_OF_TYPES.visit(getAnnotationValue(annotationMirror, elementName), elementName);
    }

    protected TypeMirror convertClassToType(
            AnnotationMirror annotationMirror, String elementName) {
        return TO_TYPE.visit(getAnnotationValue(annotationMirror, elementName));
    }

    protected List<MethodSpec> getChildMethodBuilders(List<ComponentModel> children) {

        if (children != null && children.size() > 0) {

            List<MethodSpec> methods = new ArrayList<MethodSpec>(children.size());

            for (ComponentModel child : children) {

                String name = child.name;

                methods.add(MethodSpec.methodBuilder(Character.toLowerCase(name.charAt(0)) + name.substring(1) + "Builder")
                        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
                        .returns(ClassName.get(child.packageName, name, "Builder"))
                        .build());

            }

            return methods;

        } else {
            return Collections.emptyList();
        }

    }

    private static final AnnotationValueVisitor<ImmutableList<TypeMirror>, String> TO_LIST_OF_TYPES =
            new SimpleAnnotationValueVisitor6<ImmutableList<TypeMirror>, String>() {
                @Override
                public ImmutableList<TypeMirror> visitArray(
                        List<? extends AnnotationValue> vals, String elementName) {
                    return FluentIterable.from(vals)
                            .transform(
                                    new Function<AnnotationValue, TypeMirror>() {
                                        @Override
                                        public TypeMirror apply(AnnotationValue typeValue) {
                                            return TO_TYPE.visit(typeValue);
                                        }
                                    })
                            .toList();
                }

                @Override
                protected ImmutableList<TypeMirror> defaultAction(Object o, String elementName) {
                    throw new IllegalArgumentException(elementName + " is not an array: " + o);
                }
            };


    private static final AnnotationValueVisitor<TypeMirror, Void> TO_TYPE =
            new SimpleAnnotationValueVisitor6<TypeMirror, Void>() {
                @Override
                public TypeMirror visitType(TypeMirror t, Void p) {
                    return t;
                }

                @Override
                protected TypeMirror defaultAction(Object o, Void p) {
                    throw new TypeNotPresentException(o.toString(), null);
                }
            };

}
