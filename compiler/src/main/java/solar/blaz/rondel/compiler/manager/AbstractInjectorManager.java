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

import com.google.common.base.Function;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.TypeName;
import dagger.Module;

import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.SimpleAnnotationValueVisitor6;
import javax.lang.model.util.Types;
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

    protected TypeElement parseViewComponent(TypeMirror componentClass) {

        TypeElement voidElement = elementUtils.getTypeElement(Void.class.getCanonicalName());

        if (componentClass == null || typesUtil.isSubtype(componentClass, voidElement.asType())) {
            messager.error("App component was no provided.");
        } else {

            TypeElement typeElement = elementUtils.getTypeElement(componentClass.toString());

            if (typeElement.getKind() == ElementKind.INTERFACE) {
                return typeElement;
            } else {
                messager.error("Component has to be interface.", typeElement);
            }

        }

        return null;

    }

    protected TypeElement[] parseModuleElements(ImmutableList<TypeMirror> modules) {
        if (modules == null || modules.size() == 0) {
            messager.error("App module was not provided.");
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

    protected String formatBuilderModule(TypeElement[] moduleElements, List<Object> formatParams, TypeMirror injectedInstance) {

        StringBuilder builder = new StringBuilder();

        for (TypeElement module : moduleElements) {
            TypeName moduleName = TypeName.get(module.asType());
            String moduleMethodName = module.getSimpleName().toString();
            moduleMethodName = Character.toLowerCase(moduleMethodName.charAt(0)) + moduleMethodName.substring(1);

            ExecutableElement modelConstructor = getConstructor(module, injectedInstance);

            if (modelConstructor != null) {
                if (modelConstructor.getParameters().size() > 0) {
                    builder.append("        .$L(new $T(injectie))\n");
                    formatParams.add(moduleMethodName);
                    formatParams.add(moduleName);
                }
            } else {
                messager.error("No valid constructor for module.");
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
