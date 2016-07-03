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

public class FragmentInjectorTest {


    @Test
    public void testNoParent() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends Activity {\n"
                + "    \n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import android.content.Context;\n"
                + "import android.util.AttributeSet;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
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
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    private static TestFragmentModule testFragmentModule;\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getActivity().getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .testFragmentModule(getTestFragmentModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestFragmentModule(TestFragmentModule module) {\n"
                + "        testFragmentModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestFragmentModule getTestFragmentModule(TestFragment injectie) {\n"
                + "        if (testFragmentModule != null) {\n"
                + "            return testFragmentModule;\n"
                + "        } else {\n"
                + "            return new TestFragmentModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestFragmentModule.class }\n"
                + ")\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent, TestFragmentComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testFragmentModule(TestFragmentModule module);\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testParentNotComponentProvider() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App; \n"
                + "\n"
                + ""
                + "@App\n"
                + "public class TestApp extends Application implements AppComponentProvider {\n"
                + "    public RondelAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends Activity {\n"
                + "    \n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import android.content.Context;\n"
                + "import android.util.AttributeSet;\n"
                + "import android.view.View;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Parent does not provide component.");

    }

    @Test
    public void testTestApplicationParent() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.TestApp;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.TestApp.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
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
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    private static TestFragmentModule testFragmentModule;\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getActivity().getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .testFragmentModule(getTestFragmentModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestFragmentModule(TestFragmentModule module) {\n"
                + "        testFragmentModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestFragmentModule getTestFragmentModule(TestFragment injectie) {\n"
                + "        if (testFragmentModule != null) {\n"
                + "            return testFragmentModule;\n"
                + "        } else {\n"
                + "            return new TestFragmentModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestFragmentModule.class }\n"
                + ")\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent, TestFragmentComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testFragmentModule(TestFragmentModule module);\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testTestActivityParent() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends Activity implements ComponentProvider {\n"
                + "\n"
                + "    @Override\n"
                + "    public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.ui.RondelTestActivityComponent;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    private static TestFragmentModule testFragmentModule;\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestActivity parent = (TestActivity) injectie.getActivity();\n"
                + "        RondelTestActivityComponent baseComponent = (RondelTestActivityComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .testFragmentModule(getTestFragmentModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestFragmentModule(TestFragmentModule module) {\n"
                + "        testFragmentModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestFragmentModule getTestFragmentModule(TestFragment injectie) {\n"
                + "        if (testFragmentModule != null) {\n"
                + "            return testFragmentModule;\n"
                + "        } else {\n"
                + "            return new TestFragmentModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestFragmentModule.class }\n"
                + ")\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent, TestFragmentComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testFragmentModule(TestFragmentModule module);\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testTestFragmentParent() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends Activity implements ComponentProvider {\n"
                + "\n"
                + "    @Override\n"
                + "    public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestParentFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Rondel(\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestParentFragment extends Fragment implements ComponentProvider {\n"
                + "\n"
                + "    @Override public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentParentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.ui.fragment.TestParentFragment.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    private static TestFragmentModule testFragmentModule;\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestParentFragment parent = (TestParentFragment) injectie.getParentFragment();\n"
                + "        RondelTestParentFragmentComponent baseComponent = (RondelTestParentFragmentComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .testFragmentModule(getTestFragmentModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestFragmentModule(TestFragmentModule module) {\n"
                + "        testFragmentModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestFragmentModule getTestFragmentModule(TestFragment injectie) {\n"
                + "        if (testFragmentModule != null) {\n"
                + "            return testFragmentModule;\n"
                + "        } else {\n"
                + "            return new TestFragmentModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestFragmentModule.class }\n"
                + ")\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent, TestFragmentComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testFragmentModule(TestFragmentModule module);\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentParentFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testTestInvalidParent() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App; \n"
                + "\n"
                + ""
                + "@App\n"
                + "public class TestApp extends Application implements AppComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.app.Activity;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends Activity implements ComponentProvider {\n"
                + "\n"
                + "    @Override\n"
                + "    public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");


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


        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.ui.view.TestView.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, viewFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .failsToCompile()
                .withErrorContaining("Unknown parent type");

    }

    @Test
    public void testNoParams() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n" +
                "\n" +
                "import android.app.Activity;\n" +
                "import solar.blaz.rondel.Rondel;\n" +
                "\n" +
                "@Rondel\n" +
                "public class TestActivity extends Activity {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.app.Fragment;\n"
                + "import android.content.Context;\n"
                + "import android.util.AttributeSet;\n"
                + "import android.view.View;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestFragment extends Fragment {\n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
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
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestApp parent = (TestApp) injectie.getActivity().getApplicationContext();\n"
                + "        RondelTestAppComponent baseComponent = (RondelTestAppComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");


        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

    @Test
    public void testTestSupportFragment() throws Exception {

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import android.app.Application;\n"
                + "import solar.blaz.rondel.App;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "\n"
                + "@App\n"
                + "public class TestApp extends Application implements ComponentProvider {\n"
                + "    public RondelTestAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject activityFile = JavaFileObjects.forSourceString("test.ui.TestActivity", "package test.ui;\n"
                + "\n"
                + "import android.support.v4.app.FragmentActivity;\n"
                + "\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "import solar.blaz.rondel.ComponentProvider;\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "\n"
                + "@Rondel\n"
                + "public class TestActivity extends FragmentActivity implements ComponentProvider {\n"
                + "\n"
                + "    @Override\n"
                + "    public RondelComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "}");

        JavaFileObject fragmentModuleFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentModule", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class TestFragmentModule {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentComponentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragmentComponent", "package test.ui.fragment;\n" +
                "\n" +
                "import dagger.Component;\n" +
                "\n" +
                "@Component\n" +
                "public interface TestFragmentComponent {\n" +
                "    \n" +
                "}");

        JavaFileObject fragmentFile = JavaFileObjects.forSourceString("test.ui.fragment.TestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import android.support.v4.app.Fragment;\n"
                + "\n"
                + "import solar.blaz.rondel.Rondel;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Rondel(\n"
                + "        components = TestFragmentComponent.class,\n"
                + "        modules = TestFragmentModule.class,\n"
                + "        parent = test.ui.TestActivity.class\n"
                + ")\n"
                + "public class TestFragment extends Fragment {\n"
                + "    \n"
                + "}");

        JavaFileObject expectedInjector = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragment", "package test.ui.fragment;\n"
                + "\n"
                + "import javax.annotation.Generated;\n"
                + "\n"
                + "import test.ui.RondelTestActivityComponent;\n"
                + "import test.ui.TestActivity;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "class RondelTestFragment {\n"
                + "    \n"
                + "    private static TestFragmentModule testFragmentModule;\n"
                + "    \n"
                + "    public static RondelTestFragmentComponent inject(TestFragment injectie) {\n"
                + "        TestActivity parent = (TestActivity) injectie.getActivity();\n"
                + "        RondelTestActivityComponent baseComponent = (RondelTestActivityComponent) parent.getComponent();\n"
                + "        RondelTestFragmentComponent component = baseComponent.rondelTestFragmentComponentBuilder()\n"
                + "                .testFragmentModule(getTestFragmentModule(injectie))\n"
                + "                .build();\n"
                + "        component.inject(injectie);\n"
                + "        return component;\n"
                + "    }\n"
                + "    \n"
                + "    public static void setTestFragmentModule(TestFragmentModule module) {\n"
                + "        testFragmentModule = module;\n"
                + "    }\n"
                + "    \n"
                + "    private static TestFragmentModule getTestFragmentModule(TestFragment injectie) {\n"
                + "        if (testFragmentModule != null) {\n"
                + "            return testFragmentModule;\n"
                + "        } else {\n"
                + "            return new TestFragmentModule();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject expectedComponent = JavaFileObjects.forSourceString("test.ui.fragment.RondelTestFragmentComponent", "package test.ui.fragment;\n"
                + "\n"
                + "import dagger.Subcomponent;\n"
                + "import javax.annotation.Generated;\n"
                + "import solar.blaz.rondel.FragmentScope;\n"
                + "import solar.blaz.rondel.RondelComponent;\n"
                + "\n"
                + "@Generated(\n"
                + "        value = \"solar.blaz.rondel.compiler.RondelProcessor\",\n"
                + "        comments = \"http://blaz.solar/rondel/\"\n"
                + ")\n"
                + "@Subcomponent(\n"
                + "        modules = { TestFragmentModule.class }\n"
                + ")\n"
                + "@FragmentScope\n"
                + "public interface RondelTestFragmentComponent extends RondelComponent, TestFragmentComponent {\n"
                + "    \n"
                + "    void inject(TestFragment view);\n"
                + "    \n"
                + "    @Subcomponent.Builder\n"
                + "    interface Builder {\n"
                + "        Builder testFragmentModule(TestFragmentModule module);\n"
                + "        RondelTestFragmentComponent build();\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, activityFile, fragmentModuleFile, fragmentComponentFile, fragmentFile))
                .processedWith(new RondelProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(expectedInjector, expectedComponent);

    }

}
