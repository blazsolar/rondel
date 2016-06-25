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
 * Created by blazsolar on 02/03/16.
 */
public class SingletonInjectorTest {

    @Test
    public void testNoSingletonInjector() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject rondelFile = JavaFileObjects.forSourceString("test.SampleActivity", "package test;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel\n" +
                "public class SampleActivity extends Activity {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, rondelFile))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("No App level view provided.");

    }

    @Test
    public void testMultipleAppComponents() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject app2File = JavaFileObjects.forSourceString("test.App2", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App\n" +
                "public class App2 {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, app2File))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("Only one App level view is allowed.");

    }

    @Test
    public void testNoAppComponent() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        modules = test.AppModule.class\n" +
                ")\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.RondelAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { AppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelAppComponent extends BaseAppComponent {\n" +
                "    void inject(App app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(componentFile);

    }

    @Test
    public void testAppComponentNotInterface() throws Exception {

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

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("Component has to be interface.");

    }

    @Test
    public void testAppModuleNotProvided() throws Exception {

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.AppComponent", "package test;\n" +
                "\n" +
                "public interface AppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.AppComponent.class\n" +
                ")\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelApp", "package test;\n" +
                "\n" +
                "public class RondelApp {\n" +
                "    \n" +
                "    public static RondelAppComponent inject(App injectie) {\n" +
                "        RondelAppComponent component = DaggerRondelAppComponent.builder()\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "\n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component\n" +
                "@Singleton\n" +
                "public interface RondelAppComponent extends BaseAppComponent, AppComponent {\n" +
                "    void inject(App app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedComponent, expectedInjector);

    }

    @Test
    public void testAppModuleHasNoAnnotation() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "public class AppModule {\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.AppComponent", "package test;\n" +
                "\n" +
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

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor())
                .failsToCompile()
                .withErrorContaining("App module is missing @Module annotation.");

    }

    @Test
    public void testAppModule() throws Exception {

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

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelApp", "package test;\n" +
                "\n" +
                "public class RondelApp {\n" +
                "    \n" +
                "    private static AppModule appModule;\n" +
                "    \n" +
                "    public static RondelAppComponent inject(App injectie) {\n" +
                "        RondelAppComponent component = DaggerRondelAppComponent.builder()\n" +
                "                .appModule(getAppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "\n" +
                "    public static void setAppModule(AppModule module) {\n" +
                "        appModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static AppModule getAppModule(App injectie) {\n" +
                "        if (appModule != null) {\n" +
                "            return appModule;\n" +
                "        } else {\n" +
                "            return new AppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { AppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelAppComponent extends BaseAppComponent, AppComponent {\n" +
                "    void inject(App app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testAppComponent() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "    \n" +
                "    public TestAppModule(TestApp app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = test.TestAppModule.class\n" +
                ")\n" +
                "public class TestApp {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelTestApp", "package test;\n" +
                "\n" +
                "public class RondelTestApp {\n" +
                "    \n" +
                "    private static TestAppModule testAppModule;\n" +
                "    \n" +
                "    public static RondelTestAppComponent inject(TestApp injectie) {\n" +
                "        RondelTestAppComponent component = DaggerRondelTestAppComponent.builder()\n" +
                "                .testAppModule(getTestAppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTestAppModule(TestAppModule module) {\n" +
                "        testAppModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static TestAppModule getTestAppModule(TestApp injectie) {\n" +
                "        if (testAppModule != null) {\n" +
                "            return testAppModule;\n" +
                "        } else {\n" +
                "            return new TestAppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelTestAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { TestAppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelTestAppComponent extends BaseAppComponent, TestAppComponent {\n" +
                "    void inject(TestApp app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testNoConstructorModule() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = test.TestAppModule.class\n" +
                ")\n" +
                "public class TestApp {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelTestApp", "package test;\n" +
                "\n" +
                "public class RondelTestApp {\n" +
                "    \n" +
                "    private static TestAppModule testAppModule;\n" +
                "    \n" +
                "    public static RondelTestAppComponent inject(TestApp injectie) {\n" +
                "        RondelTestAppComponent component = DaggerRondelTestAppComponent.builder()\n" +
                "                .testAppModule(getTestAppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTestAppModule(TestAppModule module) {\n" +
                "        testAppModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static TestAppModule getTestAppModule(TestApp injectie) {\n" +
                "        if (testAppModule != null) {\n" +
                "            return testAppModule;\n" +
                "        } else {\n" +
                "            return new TestAppModule();\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelTestAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { TestAppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelTestAppComponent extends BaseAppComponent, TestAppComponent {\n" +
                "    void inject(TestApp app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testNoValidModuleConstructor() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "\n" +
                "    public TestAppModule(String param) {\n" +
                "    }\n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = test.TestAppModule.class\n" +
                ")\n" +
                "public class TestApp {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("No valid constructor for module.");

    }

    @Test
    public void testModuleConstructorChildType() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "\n" +
                "    public TestAppModule(BaseApp baseApp) {\n" +
                "    }\n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject baseAppFile = JavaFileObjects.forSourceString("test.BaseApp", "package test;\n" +
                "\n" +
                "public class BaseApp {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = test.TestAppModule.class\n" +
                ")\n" +
                "public class TestApp extends BaseApp {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelTestApp", "package test;\n" +
                "\n" +
                "public class RondelTestApp {\n" +
                "    \n" +
                "    private static TestAppModule testAppModule;\n" +
                "    \n" +
                "    public static RondelTestAppComponent inject(TestApp injectie) {\n" +
                "        RondelTestAppComponent component = DaggerRondelTestAppComponent.builder()\n" +
                "                .testAppModule(getTestAppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTestAppModule(TestAppModule module) {\n" +
                "        testAppModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static TestAppModule getTestAppModule(TestApp injectie) {\n" +
                "        if (testAppModule != null) {\n" +
                "            return testAppModule;\n" +
                "        } else {\n" +
                "            return new TestAppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelTestAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { TestAppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelTestAppComponent extends BaseAppComponent, TestAppComponent {\n" +
                "    void inject(TestApp app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, baseAppFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testModuleConstructorParentType() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "\n" +
                "    public TestAppModule(ExtendedApp extendedApp) {\n" +
                "    }\n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject baseAppFile = JavaFileObjects.forSourceString("test.ExtendedApp", "package test;\n" +
                "\n" +
                "public class ExtendedApp extends TestApp {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = test.TestAppModule.class\n" +
                ")\n" +
                "public class TestApp {\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, baseAppFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("No valid constructor for module.");

    }

    @Test
    public void testMultipleModules() throws Exception {


        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.TestAppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule {\n" +
                "    \n" +
                "    public TestAppModule(TestApp app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject moduleFile2 = JavaFileObjects.forSourceString("test.TestAppModule2", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestAppModule2 {\n" +
                "    \n" +
                "    public TestAppModule2(TestApp app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestAppComponent", "package test;\n" +
                "\n" +
                "public interface TestAppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.TestAppComponent.class,\n" +
                "        modules = {\n" +
                "                TestAppModule.class,\n" +
                "                TestAppModule2.class\n" +
                "        }\n" +
                ")\n" +
                "public class TestApp {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelTestApp", "package test;\n" +
                "\n" +
                "public class RondelTestApp {\n" +
                "    \n" +
                "    private static TestAppModule testAppModule;\n" +
                "    private static TestAppModule2 testAppModule2;\n" +
                "    \n" +
                "    public static RondelTestAppComponent inject(TestApp injectie) {\n" +
                "        RondelTestAppComponent component = DaggerRondelTestAppComponent.builder()\n" +
                "                .testAppModule(getTestAppModule(injectie))\n" +
                "                .testAppModule2(getTestAppModule2(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTestAppModule(TestAppModule module) {\n" +
                "        testAppModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static TestAppModule getTestAppModule(TestApp injectie) {\n" +
                "        if (testAppModule != null) {\n" +
                "            return testAppModule;\n" +
                "        } else {\n" +
                "            return new TestAppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTestAppModule2(TestAppModule2 module) {\n" +
                "        testAppModule2 = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static TestAppModule2 getTestAppModule2(TestApp injectie) {\n" +
                "        if (testAppModule2 != null) {\n" +
                "            return testAppModule2;\n" +
                "        } else {\n" +
                "            return new TestAppModule2(injectie);    \n" +
                "        }\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelTestAppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { TestAppModule.class, TestAppModule2.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelTestAppComponent extends BaseAppComponent, TestAppComponent {\n" +
                "    void inject(TestApp app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, moduleFile2, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testNaming() throws Exception {


        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.Test1AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class Test1AppModule {\n" +
                "    \n" +
                "    public Test1AppModule(Test3App app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.Test2AppComponent", "package test;\n" +
                "\n" +
                "public interface Test2AppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.Test3App", "package test;\n" +
                "\n" +
                "import solar.blaz.rondel.App;\n" +
                "\n" +
                "@App(\n" +
                "        components = test.Test2AppComponent.class,\n" +
                "        modules = {\n" +
                "                Test1AppModule.class\n" +
                "        }\n" +
                ")\n" +
                "public class Test3App {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.RondelTest3App", "package test;\n" +
                "\n" +
                "public class RondelTest3App {\n" +
                "    \n" +
                "    private static Test1AppModule test1AppModule;\n" +
                "    \n" +
                "    public static RondelTest3AppComponent inject(Test3App injectie) {\n" +
                "        RondelTest3AppComponent component = DaggerRondelTest3AppComponent.builder()\n" +
                "                .test1AppModule(getTest1AppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setTest1AppModule(Test1AppModule module) {\n" +
                "        test1AppModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static Test1AppModule getTest1AppModule(Test3App injectie) {\n" +
                "        if (test1AppModule != null) {\n" +
                "            return test1AppModule;\n" +
                "        } else {\n" +
                "            return new Test1AppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.RondelTest3AppComponent", "package test;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { Test1AppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelTest3AppComponent extends BaseAppComponent, Test2AppComponent {\n" +
                "    void inject(Test3App app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testPackageNames() throws Exception {


        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.module.AppModule", "package test.module;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "import test.app.App;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "    public AppModule(App app) {\n" +
                "        \n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.component.AppComponent", "package test.component;\n" +
                "\n" +
                "public interface AppComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.app.App", "package test.app;\n" +
                "\n" +
                "@solar.blaz.rondel.App(\n" +
                "        components = test.component.AppComponent.class,\n" +
                "        modules = test.module.AppModule.class\n" +
                ")\n" +
                "public class App {\n" +
                "    \n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.app.RondelApp", "package test.app;\n" +
                "\n" +
                "import test.module.AppModule;\n" +
                "\n" +
                "public class RondelApp {\n" +
                "    \n" +
                "    private static AppModule appModule;\n" +
                "    \n" +
                "    public static RondelAppComponent inject(App injectie) {\n" +
                "        RondelAppComponent component = DaggerRondelAppComponent.builder()\n" +
                "                .appModule(getAppModule(injectie))\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "    public static void setAppModule(AppModule module) {\n" +
                "        appModule = module;\n" +
                "    }\n" +
                "    \n" +
                "    private static AppModule getAppModule(App injectie) {\n" +
                "        if (appModule != null) {\n" +
                "            return appModule;\n" +
                "        } else {\n" +
                "            return new AppModule(injectie);\n" +
                "        }\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.app.RondelAppComponent", "package test.app;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "import javax.inject.Singleton;\n" +
                "import solar.blaz.rondel.BaseAppComponent;\n" +
                "import test.component.AppComponent;\n" +
                "import test.module.AppModule\n" +
                "\n" +
                "@Component(\n" +
                "        modules = { AppModule.class }\n" +
                ")\n" +
                "@Singleton\n" +
                "public interface RondelAppComponent extends BaseAppComponent, AppComponent {\n" +
                "    void inject(App app);\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);


    }
}
