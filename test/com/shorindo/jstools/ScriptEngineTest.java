/*
 * Copyright 2015 Shorindo, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.shorindo.jstools;

import static org.junit.Assert.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.junit.Test;

/**
 * 
 */
public class ScriptEngineTest {

    @Test
    public void test() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
//        engine.put("test", new TestGetter());
//        assertEquals("string", (String)engine.eval("test.string"));
//        assertEquals(123, (int)engine.eval("test.intValue"));
//        System.out.println(engine.eval("typeof test.intValue"));
        
        engine.put("json", "{\"i\":123,\"f\":123.456,\"s\":\"string\",\"o\":{\"name\":\"foo\"}}");
        engine.eval("var o = JSON.parse(json);");
        engine.put("Map", HashMap.class);
//        engine.eval("importPackage(java.utl); map = new java.util.HashMap();");
//        System.out.println(engine.eval("o.i"));
//        System.out.println(engine.eval("o.f"));
//        System.out.println(engine.eval("o.s"));
//        System.out.println(engine.eval("o.o.name"));
//        System.out.println(engine.eval("Object.keys(o)"));
        TestSetter target = new TestSetter();
        engine.put("util", this);
        engine.put("target", target);
        engine.eval(
            "function js2java(o, target) {\n" +
            "    for (var key in o) {\n" +
            "        if (typeof o[key] == 'object') {\n" +
            "            target[key] = util.getSetterClassForName(key).newInstance();\n" +
            "            js2java(o[key], target[key]);\n" +
            "        } else {\n" +
            "            target[key] = o[key];\n" +
            "        }\n" +
            "    }\n" +
            "}\n" +
            "js2java(o, target);"
        );
        engine.eval("print('hello\\n');");
        System.out.println(target);
    }
    
    @SuppressWarnings("serial")
    @Test
    public void testGenerics() throws Exception {
        List<String> list = new ArrayList<String>(){};
        Type type = list.getClass().getGenericSuperclass();
        System.out.println(((ParameterizedType)type).getActualTypeArguments()[0]);
        System.out.println((new String()).getClass().getGenericSuperclass());
//        System.out.println(list.getClass().getComponentType());
//        System.out.println(list.getClass());
    }
    
    public Class<?> getSetterClassForName(String name) {
        return TestSetterSub.class;
    }

    public static class TestGetter {
        private int i = 123;
        private String s = "string";
        public int getIntValue() {
            return i;
        }
        public String getString() {
            return s;
        }
    }
    
    public static class TestSetter {
        private int i;
        private float f;
        private String s;
        private TestSetterSub o;
        public int getI() {
            return i;
        }
        public void setI(int i) {
            this.i = i;
        }
        public float getF() {
            return f;
        }
        public void setF(float f) {
            this.f = f;
        }
        public String getS() {
            return s;
        }
        public void setS(String s) {
            this.s = s;
        }
        public TestSetterSub getO() {
            return o;
        }
        public void setO(TestSetterSub o) {
            this.o = o;
        }
        public String toString() {
            return "i=" + i + ", f=" + f + ", s=" + s + ", o=" + o;
        }
    }
    
    public static class TestSetterSub {
        private String name;
        public String getName() {
            return name;
        }
        public void setName(String name) {
            this.name = name;
        }
        public String toString() {
            return name;
        }
    }
}
