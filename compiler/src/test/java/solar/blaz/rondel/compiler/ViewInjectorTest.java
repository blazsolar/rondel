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
 * Created by blazsolar on 06/03/16.
 */
public class ViewInjectorTest {

    @Test
    public void testNoParent() throws Exception {

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
                "    public MVPAppComponent getComponent() {\n" +
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
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject viewModuleFile = JavaFileObjects.forSourceString("test.ui.view.TestViewModule", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestViewModule {\n" +
                "    \n" +
                "}");

        JavaFileObject viewComponentFile = JavaFileObjects.forSourceString("test.ui.view.TestViewComponent", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestViewComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject viewFile = JavaFileObjects.forSourceString("test.ui.view.TestView", "package test.ui.view;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import android.content.Context;\n" +
                "import android.util.AttributeSet;\n" +
                "import android.view.View;\n" +
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestViewComponent.class,\n" +
                "        modules = TestViewModule.class\n" +
                ")\n" +
                "public class TestView extends View {\n" +
                "    public TestView(Context context, AttributeSet attrs) {\n" +
                "        super(context, attrs);\n" +
                "    }\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile,
                        activityComponentFile, viewModuleFile, viewComponentFile, viewFile))
                .processedWith(new DaggerMVPProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("View has to specify parent.");

    }

    @Test
    public void testParentNotComponentProvider() throws Exception {

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
                "    public MVPAppComponent getComponent() {\n" +
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
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject viewModuleFile = JavaFileObjects.forSourceString("test.ui.view.TestViewModule", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestViewModule {\n" +
                "    \n" +
                "}");

        JavaFileObject viewComponentFile = JavaFileObjects.forSourceString("test.ui.view.TestViewComponent", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestViewComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject viewFile = JavaFileObjects.forSourceString("test.ui.view.TestView", "package test.ui.view;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import android.content.Context;\n" +
                "import android.util.AttributeSet;\n" +
                "import android.view.View;\n" +
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestViewComponent.class,\n" +
                "        modules = TestViewModule.class,\n" +
                "        parent = test.ui.TestActivity.class\n" +
                ")\n" +
                "public class TestView extends View {\n" +
                "    public TestView(Context context, AttributeSet attrs) {\n" +
                "        super(context, attrs);\n" +
                "    }\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile,
                        activityComponentFile, viewModuleFile, viewComponentFile, viewFile))
                .processedWith(new DaggerMVPProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Parent does not provide component.");

    }

    @Test
    public void testView() throws Exception {

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
                "    public MVPTestAppComponent getComponent() {\n" +
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
                "import solar.blaz.rondel.BaseComponent;\n" +
                "import solar.blaz.rondel.ComponentProvider;\n" +
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity implements ComponentProvider {\n" +
                "\n" +
                "    @Override\n" +
                "    public BaseComponent getComponent() {\n" +
                "        return null;\n" +
                "    }\n" +
                "}");

        JavaFileObject viewModuleFile = JavaFileObjects.forSourceString("test.ui.view.TestViewModule", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestViewModule {\n" +
                "    \n" +
                "}");

        JavaFileObject viewComponentFile = JavaFileObjects.forSourceString("test.ui.view.TestViewComponent", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestViewComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject viewFile = JavaFileObjects.forSourceString("test.ui.view.TestView", "package test.ui.view;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import android.content.Context;\n" +
                "import android.util.AttributeSet;\n" +
                "import android.view.View;\n" +
                "import solar.blaz.rondel.Mvp;\n" +
                "\n" +
                "@Mvp(\n" +
                "        components = TestViewComponent.class,\n" +
                "        modules = TestViewModule.class,\n" +
                "        parent = test.ui.TestActivity.class\n" +
                ")\n" +
                "public class TestView extends View {\n" +
                "    public TestView(Context context, AttributeSet attrs) {\n" +
                "        super(context, attrs);\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.view.MVPTestView", "package test.ui.view;\n" +
                "\n" +
                "import test.ui.MVPTestActivityComponent;\n" +
                "import test.ui.TestActivity;\n" +
                "\n" +
                "class MVPTestView {\n" +
                "    \n" +
                "    public static MVPTestViewComponent inject(TestView injectie) {\n" +
                "        TestActivity activity = (TestActivity) injectie.getContext();\n" +
                "        MVPTestActivityComponent baseComponent = (MVPTestActivityComponent) activity.getComponent();\n" +
                "        MVPTestViewComponent component = baseComponent.mVPTestViewComponentBuilder()\n" +
                "                .build();\n" +
                "        component.inject(injectie);\n" +
                "        return component;\n" +
                "    }\n" +
                "    \n" +
                "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.view.MVPTestViewComponent", "package test.ui.view;\n" +
                "\n" +
                "import dagger.Subcomponent;\n" +
                "import solar.blaz.rondel.BaseComponent;\n" +
                "import solar.blaz.rondel.ViewScope;\n" +
                "\n" +
                "@Subcomponent(\n" +
                "        modules = { TestViewModule.class }\n" +
                ")\n" +
                "@ViewScope\n" +
                "public interface MVPTestViewComponent extends BaseComponent, TestViewComponent {\n" +
                "    \n" +
                "    void inject(TestView view);\n" +
                "    \n" +
                "    @Subcomponent.Builder\n" +
                "    interface Builder {\n" +
                "        Builder testViewModule(TestViewModule module);\n" +
                "        MVPTestViewComponent build();\n" +
                "    }\n" +
                "    \n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile,
                        activityComponentFile, viewModuleFile, viewComponentFile, viewFile))
                .processedWith(new DaggerMVPProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

}
