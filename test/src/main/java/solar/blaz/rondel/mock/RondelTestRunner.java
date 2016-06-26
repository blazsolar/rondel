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

package solar.blaz.rondel.mock;

import android.app.Application;
import android.content.Context;
import android.support.test.runner.AndroidJUnitRunner;
import android.view.View;
import android.view.ViewParent;

import java.lang.reflect.Field;

/**
 * Created by blaz on 08/06/16.
 */
public class RondelTestRunner extends AndroidJUnitRunner {

    private String mockClassName;

    public RondelTestRunner() {
        super();

        try {
            Class<?> aClass = Class.forName("solar.blaz.rondel.Rondel");
            Field field = aClass.getField("MOCK_CLASS_NAME");
            mockClassName = (String) field.get(null);
        } catch (ClassNotFoundException e) {
            mockClassName = null;
        } catch (NoSuchFieldException e) {
            mockClassName = null;
        } catch (IllegalAccessException e) {
            mockClassName = null;
        }

    }

    @Override public Application newApplication(ClassLoader cl, String className,
            Context context) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        return super.newApplication(cl, getClassName(className), context);
    }

    private String getClassName(String originalName) {
        if (mockClassName != null) {
            return mockClassName;
        } else {
            return originalName;
        }
    }

    private static View getParrent(View view) {
        if (view instanceof View) {
            return view;
        } else {
            ViewParent parent = view.getParent();
            if (parent == null) {
                throw new IllegalStateException("Parent not found");
            } else{
                return getParrent(view);
            }
        }
    }

}
