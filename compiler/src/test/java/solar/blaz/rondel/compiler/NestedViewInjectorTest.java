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
 * Created by blazsolar on 06/03/16.
 */
public class NestedViewInjectorTest {

    @Test
    public void testNestedViews() throws Exception {

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
                "import solar.blaz.rondel.ComponentProvider;\n" +
                "\n" +
                "@App(\n" +
                "        components = AppComponent.class,\n" +
                "        modules = AppModule.class\n" +
                ")\n" +
                "public class TestApp extends Application implements ComponentProvider {\n" +
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
                "import solar.blaz.rondel.RondelComponent;\n" +
                "import solar.blaz.rondel.ComponentProvider;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity implements ComponentProvider {\n" +
                "\n" +
                "    @Override\n" +
                "    public RondelComponent getComponent() {\n" +
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

        JavaFileObject viewFile = JavaFileObjects.forSourceString("test.ui.view.TestView", "package test.ui.view;\n"
                + "\n"
                + "import android.content.Context;\n"
                + "import android.util.AttributeSet;\n"
                + "import android.widget.LinearLayout;\n"
                + "\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestViewComponent.class,\n"
                + "        modules = TestViewModule.class,\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestView extends LinearLayout implements ComponentProvider {\n"
                + "    public TestView(Context context, AttributeSet attrs) {\n"
                + "        super(context, attrs);\n"
                + "    }\n"
                + "    \n"
                + "    @Override public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject subViewFile = JavaFileObjects.forSourceString("test.ui.view.sub.TestSubView", "package test.ui.view.sub;\n" +
                "\n" +
                "" +
                "import android.content.Context;\n" +
                "import android.util.AttributeSet;\n" +
                "import android.view.View;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "import test.ui.view.TestView;\n" +
                "\n" +
                "@Rondel(\n" +
                "        parent = test.ui.view.TestView.class\n" +
                ")\n" +
                "public class TestSubView extends View {\n" +
                "    public TestSubView(Context context, AttributeSet attrs) {\n" +
                "        super(context, attrs);\n" +
                "    }\n" +
                "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.view.sub.RondelTestSubView", "package test.ui.view.sub;\n"
                + "\n"
                + "import android.view.ViewParent;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.ui.view.RondelTestViewComponent;\n"
                + "import test.ui.view.TestView;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestSubView {\n"
                + "    \n"
                + "    private static TestView getParent(ViewParent view) {\n"
                + "        if (view instanceof TestView) {\n"
                + "            return (TestView) view;\n"
                + "        } else {\n"
                + "            ViewParent parent = view.getParent();\n"
                + "            if (parent == null) {\n"
                + "                throw new IllegalStateException(\"Parent not found\");\n"
                + "            } else {\n"
                + "                return getParent(parent);\n"
                + "            }\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "    public static RondelTestSubViewComponent inject(TestSubView injectie) {\n"
                + "        TestView parent = (TestView) getParent(injectie.getParent());\n"
                + "        RondelTestViewComponent baseComponent = (RondelTestViewComponent) parent.getComponent();\n"
                + "        RondelTestSubViewComponent component = baseComponent.rondelTestSubViewComponentBuilder()\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.view.sub.RondelSubTestViewComponent", "package test.ui.view.sub;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ViewScope;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent\n"
                + "@ViewScope\n"
                + "public interface RondelTestSubViewComponent extends RondelComponent {\n"
                + "    \n"
                + "    void inject(TestSubView view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        RondelTestSubViewComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile,
                        activityComponentFile, viewModuleFile, viewComponentFile, viewFile, subViewFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testParentNotProvider() throws Exception {

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
                "import solar.blaz.rondel.App; \n" +
                "\n" +
                "" +
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
                "import solar.blaz.rondel.RondelComponent;\n" +
                "import solar.blaz.rondel.ComponentProvider;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel(\n" +
                "        components = TestComponent.class,\n" +
                "        modules = TestModule.class\n" +
                ")\n" +
                "public class TestActivity extends Activity implements ComponentProvider {\n" +
                "\n" +
                "    @Override\n" +
                "    public RondelComponent getComponent() {\n" +
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

        JavaFileObject viewFile = JavaFileObjects.forSourceString("test.ui.view.TestView", "package test.ui.view;\n"
                + "\n"
                + "import android.content.Context;\n"
                + "import android.util.AttributeSet;\n"
                + "import android.widget.LinearLayout;\n"
                + "\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestViewComponent.class,\n"
                + "        modules = TestViewModule.class,\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestView extends LinearLayout {\n"
                + "    public TestView(Context context, AttributeSet attrs) {\n"
                + "        super(context, attrs);\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject subViewFile = JavaFileObjects.forSourceString("test.ui.view.sub.TestSubView", "package test.ui.view.sub;\n" +
                "\n" +
                "" +
                "import android.content.Context;\n" +
                "import android.util.AttributeSet;\n" +
                "import android.view.View;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "import test.ui.view.TestView;\n" +
                "\n" +
                "@Rondel(\n" +
                "        parent = test.ui.view.TestView.class\n" +
                ")\n" +
                "public class TestSubView extends View {\n" +
                "    public TestSubView(Context context, AttributeSet attrs) {\n" +
                "        super(context, attrs);\n" +
                "    }\n" +
                "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile, componentFile, activityFile, activityModuleFile,
                        activityComponentFile, viewModuleFile, viewComponentFile, viewFile, subViewFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Parent does not provide component.");

    }

}
