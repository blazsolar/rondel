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

package solar.blaz.rondel.compiler.manager;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

/**
 * Created by blazsolar on 05/03/16.
 */
@Singleton
public class Messager {

    private final javax.annotation.processing.Messager messager;

    @Inject
    public Messager(javax.annotation.processing.Messager messager) {
        this.messager = messager;
    }

    public void error(String message) {
        error(message, null);
    }

    public void error(String message, Element element) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    public void warning(String message) {
        messager.printMessage(Diagnostic.Kind.WARNING, message);
    }

}
