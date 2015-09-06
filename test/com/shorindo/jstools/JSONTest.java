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

import java.lang.reflect.GenericDeclaration;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.shorindo.jstools.JSON;

/**
 * 
 */
public class JSONTest {
    @Test
    public void testStringifyNull() throws Exception {
        assertEquals("null", JSON.stringify(null));
    }
    @Test
    public void testStringifyBoolean() throws Exception {
        assertEquals("true", JSON.stringify(true));
        assertEquals("false", JSON.stringify(false));
    }
    @Test
    public void testStringifyNumber() throws Exception {
        assertEquals("123", JSON.stringify((short)123));
        assertEquals("123", JSON.stringify(new Short((short)123)));
        assertEquals("123", JSON.stringify((int)123));
        assertEquals("123", JSON.stringify(new Integer(123)));
        assertEquals("123", JSON.stringify((long)123));
        assertEquals("123", JSON.stringify(new Long(123)));
        assertEquals("123.456", JSON.stringify((float)123.456));
        assertEquals("123.456", JSON.stringify(new Float(123.456)));
        assertEquals("123.456", JSON.stringify((double)123.456));
        assertEquals("123.456", JSON.stringify(new Double(123.456)));
    }
    @Test
    public void testStringifyString() throws Exception {
        assertEquals("\"abc\"", JSON.stringify("abc"));
    }
    @Test
    public void testStringifyArray() throws Exception {
        assertEquals("[]", JSON.stringify(new String[]{}));
        assertEquals("[true,false,true]", JSON.stringify(new boolean[]{ true, false, true }));
        assertEquals("[true,false,true]", JSON.stringify(new Boolean[]{ true, false, true }));
        assertEquals("[1,2,3]", JSON.stringify(new int[]{ 1, 2, 3 }));
        assertEquals("[1,2,3]", JSON.stringify(new Integer[]{ 1, 2, 3 }));
        assertEquals("[1,2,3]", JSON.stringify(new long[]{ 1, 2, 3 }));
        assertEquals("[1,2,3]", JSON.stringify(new Long[]{ (long)1, (long)2, (long)3 }));
        assertEquals("[1.2,2.3,3.4]", JSON.stringify(new float[]{ (float)1.2, (float)2.3, (float)3.4 }));
        assertEquals("[1.2,2.3,3.4]", JSON.stringify(new Float[]{ (float)1.2, (float)2.3, (float)3.4 }));
        assertEquals("[1.2,2.3,3.4]", JSON.stringify(new double[]{ (double)1.2, (double)2.3, (double)3.4 }));
        assertEquals("[1.2,2.3,3.4]", JSON.stringify(new Double[]{ (double)1.2, (double)2.3, (double)3.4 }));
        TestClass[] array = new TestClass[3];
        array[0] = new TestClass() {
            public int getI() { return 123; }
        };
        array[1] = new TestClass() {
            public double getD() { return 123.456; }
        };
        array[2] = new TestClass() {
            public String getS() { return "abc"; }
        };
        assertEquals("[{\"i\":123},{\"d\":123.456},{\"s\":\"abc\"}]", JSON.stringify(array));
    }
    @Test
    public void testStringifyObject() throws Exception {
        assertEquals("{}", JSON.stringify(new Object(){}));
        Map<String,Object> expectMap = new HashMap<String,Object>();
        expectMap.put("b", true);
        expectMap.put("i", (int)123);
        expectMap.put("f", (float)123);
        expectMap.put("d", (double)123.456);
        expectMap.put("s", "abc");
        assertEquals("{\"b\":true,\"d\":123.456,\"f\":123.0,\"i\":123,\"s\":\"abc\"}", JSON.stringify(expectMap));
    }
    @Test
    public void testParseNull() throws Exception {
        assertEquals(null, JSON.parse("null", String.class));
    }
    @Test
    public void testParseBoolean() throws Exception {
        assertEquals(true, JSON.parse("true", boolean.class));
        assertEquals(false, JSON.parse("false", Boolean.class));
    }
    @Test
    public void testParseNumber() throws Exception {
        assertEquals((Integer)1234, JSON.parse("1234", int.class));
        assertEquals((Integer)1234, JSON.parse("1234", Integer.class));
    }
    @Test
    public void testParseString() throws Exception {
        assertEquals("string", JSON.parse("\"string\"", String.class));
        assertEquals(null, JSON.parse("null", String.class));
    }
    @Test
    public void testParseArray() throws Exception {
        boolean[] booleanArray = new boolean[0];
        booleanArray = JSON.parse("[true, false, true]", booleanArray.getClass());
        assertEquals(true, booleanArray[0]);
        assertEquals(false, booleanArray[1]);
        assertEquals(true, booleanArray[2]);

        int[] intArray = new int[0];
        intArray = JSON.parse("[1, 2, 3]", intArray.getClass());
        assertEquals(1, intArray[0]);
        assertEquals(2, intArray[1]);
        assertEquals(3, intArray[2]);

        float[] floatArray = new float[0];
        floatArray = JSON.parse("[1.2 ,2.3, 3.4]", floatArray.getClass());
        assertEquals(1.2, floatArray[0], 0.01);
        assertEquals(2.3, floatArray[1], 0.01);
        assertEquals(3.4, floatArray[2], 0.01);

        String[] stringArray = new String[0];
        stringArray = JSON.parse("[\"a\",\"b\",\"c\"]", stringArray.getClass());
        assertEquals("a", stringArray[0]);
        assertEquals("b", stringArray[1]);
        assertEquals("c", stringArray[2]);
    }
    @Test
    public void testParseObject() {
        TestClass result = JSON.parse("{\"i\":123, \"d\":123.456, \"s\":\"abc\", \"o\":{\"i\":234, \"s\":\"sub\"}}", TestClass.class);
        assertEquals(123, result.getI());
        assertEquals(123.456, result.getD(), 0.0001);
        assertEquals("abc", result.getS());
        assertEquals(234, result.getO().getI());
        assertEquals("sub", result.getO().getS());
    }

    /***************************************************************************
     * 
     */
    public static class TestClass {
        private int i;
        private double d;
        private String s;
        private TestClass o;
        
        public int getI() {
            return i;
        }
        public void setI(int i) {
            this.i = i;
        }
        public double getD() {
            return d;
        }
        public void setD(double d) {
            this.d = d;
        }
        public String getS() {
            return s;
        }
        public void setS(String s) {
            this.s = s;
        }
        public TestClass getO() {
            return o;
        }
        public void setO(TestClass o) {
            this.o = o;
        }
    }
    
    /***************************************************************************
     * 
     */
    public static class Response<T> {
        private int id = 123;
        private T result;
        public int getid() {
            return id;
        }
        public void setResult(T r) {
            result = r;
        }
        @SuppressWarnings("unchecked")
        public T getResult() {
            return result;
//            Type type = getClass().getGenericSuperclass();
//            if (type instanceof ParameterizedType) {
//                Type subType = ((ParameterizedType) type).getActualTypeArguments()[0];
//                try {
//                    return (T) ((Class<?>)subType).newInstance();
//                } catch (InstantiationException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//            }
//            return result;
        }
    }
    @Test
    public void testType() {
        //型として指定するクラスは引数なしのコンストラクタを持たねばならない
        //インスタンス化するときは指定クラスを継承した匿名クラスとしてnewしなくてはならない
        Response<String> response = new Response<String>();
        response.setResult("abc");
//        System.out.println(JSON.stringify(response));
//        assertEquals("abc", response.getResult());
        JSON.parse("{\"result\":\"hoeg\"}", response.getClass());
    }
}
