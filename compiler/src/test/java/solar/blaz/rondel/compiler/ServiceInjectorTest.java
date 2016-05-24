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

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.JavaFileObjects;
import dagger.internal.codegen.ComponentProcessor;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by blaz on 05/04/16.
 */
public class ServiceInjectorTest {

    @Test
    public void testServiceRondel() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(TestApp app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.AppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface AppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import android.app.Application;\n" +
                "import solar.blaz.rondel.App;\n" +
                "import solar.blaz.rondel.AppComponentProvider;\n" +
                "\n" +
                "@App(\n" +
                "        components = AppComponent.class,\n" +
                "        modules = AppModule.class\n" +
                ")\n" +
                "public class TestApp extends Application implements AppComponentProvider {\n" +
                "    public RondelTestAppComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.service.TestModule", "package test.service;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.service.TestComponent", "package test.service;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.service.TestService", "package test.service;\n" +
                "\n" +
                "import android.app.Service;\n" +
                "import android.content.Intent;\n" +
                "import android.os.IBinder;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestService extends Service {\n" +
                "\n" +
                "    @Override\n" +
                "    public IBinder onBind(Intent intent) {\n" +
                "        return null;\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.service.RondelTestService", "package test.service;\n" +
                "\n" +
                "import test.RondelTestAppComponent;\n" +
                "import test.TestApp;\n" +
                "\n" +
                "class RondelTestService {\n" +
                "    \n" +
                "    public static RondelTestServiceComponent inject(TestService injectie) {\n" +
                "        TestApp app = (TestApp) injectie.getApplicationContext();\n" +
                "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) app.getComponent();\n" +
                "        RondelTestServiceComponent component = baseComponent.rondelTestServiceComponentBuilder()\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.service.RondelTestServiceComponent", "package test.service;\n" +
                "\n" +
                "import dagger.Subcomponent;\n" +
                "import solar.blaz.rondel.BaseComponent;\n" +
                "import solar.blaz.rondel.ViewScope;\n" +
                "\n" +
                "@Subcomponent(\n" +
                "        modules = { TestModule.class }\n" +
                ")\n" +
                "@ViewScope\n" +
                "public interface RondelTestServiceComponent extends BaseComponent, TestComponent {\n" +
                "    \n" +
                "    void inject(TestService view);\n" +
                "\n" +
                "    @Subcomponent.Builder\n" +
                "    interface Builder {\n" +
                "        Builder testModule(TestModule module);\n" +
                "        RondelTestServiceComponent build();\n" +
                "    }\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }
}
