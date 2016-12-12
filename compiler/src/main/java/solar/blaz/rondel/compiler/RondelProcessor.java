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

package solar.blaz.rondel.compiler;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.LinkedListMultimap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

import solar.blaz.rondel.App;
import solar.blaz.rondel.Rondel;
import solar.blaz.rondel.compiler.manager.Messager;
import solar.blaz.rondel.compiler.manager.SingletonInjectorManager;
import solar.blaz.rondel.compiler.manager.ViewInjectorManager;
import solar.blaz.rondel.compiler.model.ComponentModel;

/**
 * Created by blazsolar on 24/02/16.
 */
public class RondelProcessor extends AbstractProcessor {

    @Inject SingletonInjectorManager singletonInjectorManager;
    @Inject ViewInjectorManager viewInjectorManager;
    @Inject Messager messager;
    @Inject Types typesUtil;

    private LinkedListMultimap<ComponentModel, ComponentModel> children = LinkedListMultimap.create();
    private LinkedListMultimap<ComponentModel, ComponentModel> parents = LinkedListMultimap.create();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        DaggerCompilerComponent.builder()
                .compilerModule(new CompilerModule(processingEnv))
                .build()
                .inject(this);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        ComponentModel parsedComponent = singletonInjectorManager.parse(env);
        if (parsedComponent != null) {
            children.put(null, parsedComponent);
        }

        if (singletonInjectorManager.hasComponent()) {

            List<ComponentModel> componentModels = new ArrayList<ComponentModel>();

            ComponentModel appComponent = singletonInjectorManager.getComponent();

            Set<? extends Element> elements = env.getElementsAnnotatedWith(Rondel.class);

            Map<TypeMirror, ComponentModel> elementToModel = new HashMap<>();
            elementToModel.put(appComponent.element.asType(), appComponent);


            for (Element element : elements) {
                ComponentModel componentModel = viewInjectorManager.parse(element);
                if (componentModel != null) {
                    componentModels.add(componentModel);
                    elementToModel.put(componentModel.element.asType(), componentModel);
                }
            }


            for (ComponentModel componentModel : componentModels) {
                if (componentModel.parents == null || componentModel.parents.length == 0) {
                    children.put(appComponent, componentModel);
                    parents.put(componentModel, appComponent);
                } else {
                    for (TypeMirror parent : componentModel.parents) {
                        parents.put(componentModel, elementToModel.get(parent));
                        if (typesUtil.isSubtype(parent, appComponent.element.asType())) {
                            children.put(appComponent, componentModel);
                        } else {
                            for (ComponentModel child : componentModels) {
                                if (typesUtil.isSubtype(parent, child.element.asType())) {
                                    children.put(elementToModel.get(parent), componentModel);
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            try {
                singletonInjectorManager.write(children.get(appComponent));
                for (ComponentModel item : componentModels) {
                    List<ComponentModel> parent = this.parents.get(item);
                    viewInjectorManager.write(item, parent, children.get(item));
                }
            } catch (IOException e) {
                messager.warning("Failed to write files: " + e.getMessage());
            }

        }

        return false;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Rondel.class.getName(), App.class.getName());
    }

}
