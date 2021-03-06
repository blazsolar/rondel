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

package solar.blaz.rondel.test.compiler;

import com.google.common.collect.ImmutableList;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;

import javax.tools.JavaFileObject;

import dagger.internal.codegen.ComponentProcessor;
import solar.blaz.rondel.compiler.RondelProcessor;

import static com.google.common.truth.Truth.assertAbout;
import static com.google.testing.compile.JavaSourcesSubjectFactory.javaSources;

public class RondelMockProcessorTest {

    @Test
    public void testCompiler() throws Exception {

        JavaFileObject moduleFile = JavaFileObjects.forSourceString("test.AppModule", "package test;\n" +
                "\n" +
                "import dagger.Module;\n" +
                "\n" +
                "@Module\n" +
                "public class AppModule {\n" +
                "    \n" +
                "}");

        JavaFileObject appFile = JavaFileObjects.forSourceString("test.App", "package test;\n"
                + "\n"
                + "\n"
                + "\n"
                + "@solar.blaz.rondel.App(\n"
                + "        modules = test.AppModule.class\n"
                + ")\n"
                + "public class App  {\n"
                + "    \n"
                + "    public RondelAppComponent getComponent() {\n"
                + "        return null;\n"
                + "    }\n"
                + "    \n"
                + "}");

        JavaFileObject componentFile = JavaFileObjects.forSourceString("test.TestApp", "package test;\n"
                + "\n"
                + "import java.lang.Override;\n"
                + "\n"
                + "public class TestApp extends App {\n"
                + "    \n"
                + "    private RondelAppComponent component;\n"
                + "    \n"
                + "    public void reInject() {\n"
                + "        component = RondelApp.inject(this);\n"
                + "    }\n"
                + "    \n"
                + "    @Override\n"
                + "    public RondelAppComponent getComponent() {\n"
                + "        if (component != null) {\n"
                + "            return component;\n"
                + "        } else {\n"
                + "            return super.getComponent();\n"
                + "        }\n"
                + "    }\n"
                + "    \n"
                + "}");

        assertAbout(javaSources())
                .that(ImmutableList.of(appFile, moduleFile))
                .processedWith(new RondelProcessor(), new RondelTestProcessor(), new ComponentProcessor())
                .compilesWithoutError()
                .and()
                .generatesSources(componentFile);


    }
}
