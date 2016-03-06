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
import solar.blaz.dagger.mvp.App;
import solar.blaz.dagger.mvp.Mvp;
import solar.blaz.rondel.compiler.manager.SingletonInjectorManager;
import solar.blaz.rondel.compiler.manager.ViewInjectorManager;
import solar.blaz.rondel.compiler.model.ComponentModel;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.inject.Inject;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by blazsolar on 24/02/16.
 */
public class DaggerMVPProcessor extends AbstractProcessor {

    @Inject SingletonInjectorManager singletonInjectorManager;
    @Inject ViewInjectorManager viewInjectorManager;

    private LinkedListMultimap<ComponentModel, ComponentModel> components = LinkedListMultimap.create();

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
            components.put(null, parsedComponent);
        }

        if (singletonInjectorManager.hasComponent()) {

            ComponentModel appComponent = singletonInjectorManager.getComponent();

            Set<? extends Element> elements = env.getElementsAnnotatedWith(Mvp.class);

            for (Element element : elements) {

                ComponentModel componentModel = viewInjectorManager.parse(element);
                if (componentModel != null) {
                    components.put(appComponent, componentModel);
                }

            }

            try {

                List<ComponentModel> children = components.get(appComponent);

                if (children != null && children.size() > 0) {
                    for (ComponentModel child : children) {
                        viewInjectorManager.write(child, appComponent);
                    }
                }

                singletonInjectorManager.write(children);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        return true;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(Mvp.class.getName(), App.class.getName());
    }

}