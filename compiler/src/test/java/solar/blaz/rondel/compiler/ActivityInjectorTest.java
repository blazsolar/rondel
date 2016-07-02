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

import org.junit.Test;

import javax.tools.JavaFileObject;

import dagger.internal.codegen.ComponentProcessor;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

/**
 * Created by blazsolar on 05/03/16.
 */
public class ActivityInjectorTest {

    @Test
    public void testNoComponent() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(App app) {\n" +
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

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "import android.app.Application;\n" +
                "import solar.blaz.rondel.AppComponentProvider;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.AppComponent.class,\n" +
                "        modules = test.AppModule.class\n" +
                ")\n" +
                "public class App extends Application implements AppComponentProvider {\n" +
                "    public RondelAppComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent {\n"
                + "    void inject(TestActivity view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedComponent);

    }

    @Test
    public void testNoModules() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(App app) {\n" +
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

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "import android.app.Application;\n" +
                "import solar.blaz.rondel.AppComponentProvider;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.AppComponent.class,\n" +
                "        modules = test.AppModule.class\n" +
                ")\n" +
                "public class App extends Application implements AppComponentProvider {\n" +
                "    public RondelAppComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}\n");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.App;\n"
                + "import test.RondelAppComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        App parent = (App) injectie.getApplicationContext();\n"
                + "        RondelAppComponent baseComponent = (RondelAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedComponent, expectedInjector);

    }

    @Test
    public void testComponentNotInterface() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(App app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.AppComponent", "package test;\n" +
                "\n" +
                "public class AppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.AppComponent.class,\n" +
                "        modules = test.AppModule.class\n" +
                ")\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("Component has to be interface.");

    }

    @Test
    public void testModuleNoAnnotation() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(App app) {\n" +
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

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.AppComponent.class,\n" +
                "        modules = test.AppModule.class\n" +
                ")\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("pp module is missing @Module annotation.");

    }

    @Test
    public void testActivityRondel() throws Exception {

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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    \n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testModuleConstructor() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "\n" +
                "    public TestModule(TestActivity activity) {\n" +
                "    }\n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    \n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule(injectie);\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testActivityParent() throws Exception {

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
                "    public RondelAppComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class,\n" +
                "        parent = test.TestApp.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Only Views and Fragments can specify parent.");

    }

    @Test
    public void testPackageNames() throws Exception {

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

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import android.app.Application;\n" +
                "import solar.blaz.rondel.App;\n" +
                "import solar.blaz.rondel.AppComponentProvider;\n" +
                "\n" +
                "@App(\n" +
                "        modules = AppModule.class\n" +
                ")\n" +
                "public class TestApp extends Application implements AppComponentProvider {\n" +
                "    public RondelTestAppComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}");

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.module.TestModule", "package test.ui.module;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.component.TestComponent", "package test.ui.component;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.activity.TestActivity", "package test.ui.activity;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "import test.ui.component.TestComponent;\n" +
                "import test.ui.module.TestModule;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui.activity;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "import test.ui.module.TestModule;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    \n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.activity.RondelTestActivityComponent", "package test.ui.activity;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "import test.ui.component.TestComponent;\n"
                + "import test.ui.module.TestModule;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testMultipleModules() throws Exception {

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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityModule2File = JavaFileObjects.forSourceString("test.ui.TestModule2", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule2 {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = { TestModule.class, TestModule2.class }\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    private static TestModule2 testModule2;\n"
                + "\n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .testModule2(getTestModule2(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule2(TestModule2 module) {\n"
                + "        testModule2 = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule2 getTestModule2(TestActivity injectie) {\n"
                + "        if (testModule2 != null) {\n"
                + "            return testModule2;\n"
                + "        } else {\n"
                + "            return new TestModule2();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class, TestModule2.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        Builder testModule2(TestModule2 module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityModule2File, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);


    }

    @Test
    public void testMultipleModulesWithConstructor() throws Exception {

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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "\n" +
                "    public TestModule(TestActivity activity) {\n" +
                "    }\n" +
                "}");

        JavaFileObject activityModule2File = JavaFileObjects.forSourceString("test.ui.TestModule2", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule2 {\n" +
                "\n" +
                "    public TestModule2(TestActivity activity) {\n" +
                "    }\n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = { TestModule.class, TestModule2.class }\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    private static TestModule2 testModule2;\n"
                + "\n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .testModule2(getTestModule2(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule(injectie);\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule2(TestModule2 module) {\n"
                + "        testModule2 = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule2 getTestModule2(TestActivity injectie) {\n"
                + "        if (testModule2 != null) {\n"
                + "            return testModule2;\n"
                + "        } else {\n"
                + "            return new TestModule2(injectie);\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.ActivityScope;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class, TestModule2.class }\n"
                + ")\n"
                + "@ActivityScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        Builder testModule2(TestModule2 module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityModule2File, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testCustomActivityScope() throws Exception {

        JavaFileObject scopeFile = JavaFileObjects.forSourceString("test.CustomScope", "package test;\n"
                + "\n"
                + "import java.lang.annotation.Retention;\n"
                + "import java.lang.annotation.RetentionPolicy;\n"
                + "\n"
                + "import javax.inject.Scope;\n"
                + "\n"
                + "@Scope @Retention(RetentionPolicy.RUNTIME)\n"
                + "public @interface CustomScope {\n"
                + "}\n");

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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.CustomScope;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestComponent.class,\n"
                + "        modules = TestModule.class,\n"
                + "        scope = CustomScope.class\n"
                + ")\n"
                + "public class TestActivity extends Activity {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.RondelTestActivity", "package test.ui;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.RondelTestAppComponent;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestActivity {\n"
                + "    \n"
                + "    private static TestModule testModule;\n"
                + "    \n"
                + "    public static RondelTestActivityComponent inject(TestActivity injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestActivityComponent component = baseComponent.rondelTestActivityComponentBuilder()\n"
                + "                .testModule(getTestModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestModule(TestModule module) {\n"
                + "        testModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestModule getTestModule(TestActivity injectie) {\n"
                + "        if (testModule != null) {\n"
                + "            return testModule;\n"
                + "        } else {\n"
                + "            return new TestModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.RondelTestActivityComponent", "package test.ui;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.BaseComponent;\n"
                + "import test.CustomScope;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestModule.class }\n"
                + ")\n"
                + "@CustomScope\n"
                + "public interface RondelTestActivityComponent extends BaseComponent, TestComponent {\n"
                + "    \n"
                + "    void inject(TestActivity view);\n"
                + "\n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testModule(TestModule module);\n"
                + "        RondelTestActivityComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(scopeFile, appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }


    @Test
    public void testCustomActivityScopeNotInteface() throws Exception {

        JavaFileObject scopeFile = JavaFileObjects.forSourceString("test.CustomScope", "package test;\n"
                + "\n"
                + "public class CustomScope {\n"
                + "}\n");

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

        JavaFileObject activityModuleFile = JavaFileObjects.forSourceString("test.ui.TestModule", "package test.ui;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestModule {\n" +
                "    \n" +
                "}");

        JavaFileObject activityComponentFile = JavaFileObjects.forSourceString("test.ui.TestComponent", "package test.ui;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.CustomScope;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestComponent.class,\n"
                + "        modules = TestModule.class,\n"
                + "        scope = CustomScope.class\n"
                + ")\n"
                + "public class TestActivity extends Activity {\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(scopeFile, appFile, moduleFile, componentFile, activityFile, activityModuleFile, activityComponentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Scope has to bo an annotation");

    }
}
