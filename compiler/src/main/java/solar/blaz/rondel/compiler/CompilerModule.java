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

import dagger.Module;
import dagger.Provides;
import solar.blaz.rondel.compiler.manager.ManagerModule;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.inject.Singleton;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * Created by blazsolar on 05/03/16.
 */
@Module(
        includes = ManagerModule.class
)
public class CompilerModule {

    private final ProcessingEnvironment processingEnvironment;

    public CompilerModule(ProcessingEnvironment processingEnvironment) {
        this.processingEnvironment = processingEnvironment;
    }

    @Provides @Singleton ProcessingEnvironment provideProcessingEnvironment() {
        return processingEnvironment;
    }

    @Provides @Singleton Messager provideMessager(ProcessingEnvironment environment) {
        return environment.getMessager();
    }

    @Provides @Singleton Elements provideElementsUtil(ProcessingEnvironment environment) {
        return environment.getElementUtils();
    }

    @Provides @Singleton Filer provideFiler(ProcessingEnvironment environment) {
        return environment.getFiler();
    }

    @Provides @Singleton Types provideTypesUril(ProcessingEnvironment environment) {
        return environment.getTypeUtils();
    }

}
