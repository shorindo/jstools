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

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 * 
 */
public class JSON {
    public static final String NULL = "null";
    public static final String QUOTE = "\"";
    public static final String BEGIN_ARRAY = "[";
    public static final String BEGIN_OBJECT = "{";
    public static final String END_ARRAY = "]";
    public static final String END_OBJECT = "}";
    public static final String NAME_SEPARATOR = ":";
    public static final String VALUE_SEPERATOR = ",";

    public static String stringify(Object o) {
        return jsonify(o).toString();
    }
    public static JSONable jsonify(Object o) {
        JSONable json = new JSONNull();
        if (o == null) {
            return json;
        }

        if (isSubclass(o, boolean.class) || isSubclass(o, Boolean.class)) {
            json = new JSONBoolean((Boolean)o);
        } else if (isSubclass(o, short.class) || isSubclass(o, Short.class)) {
            json = new JSONNumber((Short)o);
        } else if (isSubclass(o, int.class) || isSubclass(o, Integer.class)) {
            json = new JSONNumber((Integer)o);
        } else if (isSubclass(o, long.class) || isSubclass(o, Long.class)) {
            json = new JSONNumber((Long)o);
        } else if (isSubclass(o, float.class) || isSubclass(o, Float.class)) {
            json = new JSONNumber((Float)o);
        } else if (isSubclass(o, double.class) || isSubclass(o, Double.class)) {
            json = new JSONNumber((Double)o);
        } else if (isSubclass(o, String.class)) {
            json = new JSONString((String)o);
        } else if (o.getClass().isArray()) {
            Class<?> type = o.getClass().getComponentType();
            if (type == boolean.class) {
                json = new JSONArray((boolean[])o);
            } else if (type == short.class) {
                json = new JSONArray((short[])o);
            } else if (type == int.class) {
                json = new JSONArray((int[])o);
            } else if (type == long.class) {
                json = new JSONArray((long[])o);
            } else if (type == float.class) {
                json = new JSONArray((float[])o);
            } else if (type == double.class) {
                json = new JSONArray((double[])o);
            } else {
                json = new JSONArray((Object[])o);
            }
        } else if (isSubclass(o, List.class)) {
            json = new JSONArray((List<?>)o);
        } else if (isSubclass(o, Map.class)) {
            json = new JSONObject((Map<?, ?>)o);
        } else {
            json = new JSONObject(o);
        }
        return json;
    }
    public static <T>T parse(String json, Class<T> expectClass) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        try {
            engine.put("json", json);
            JSONable jsonable = (JSONable)engine.eval(
                    "importPackage(com.shorindo.jstools);" +
                    "var JJ = com.shorindo.jstools.JSON;" +
                    "function js2java(o) {" +
                    "    if (o == null || typeof o == 'undefined') {" +
                    "        return JJ.createNull();" +
                    "    } else if (typeof o == 'boolean') {" +
                    "        return JJ.createBoolean(o);" +
                    "    } else if (typeof o == 'number') {" +
                    "        return JJ.createNumber(o);" +
                    "    } else if (typeof o == 'string') {" +
                    "        return JJ.createString(o);" +
                    "    } else if (Array.isArray(o)) {" +
                    "        var result = JJ.createArray();" +
                    "        for (var i = 0; i < o.length; i++) {" +
                    "            result.add(js2java(o[i]));" +
                    "        }" +
                    "        return result;" +
                    "    } else if (typeof o == 'object') {" +
                    "        var result = JJ.createObject();" +
                    "        for (var key in o) {" +
                    "            result.setProperty(key, js2java(o[key]));" +
                    "        }" +
                    "        return result;" +
                    "    }" +
                    "}" +
                    "js2java(JSON.parse(json));"
                );
            System.out.println(jsonable.toString());
            return jsonable.getValue(expectClass);
        } catch (ScriptException e) {
            throw new JSONException(e);
        }
    }
    public static JSONable createNull() {
        return new JSONNull();
    }
    public static JSONable createBoolean(Boolean b) {
        return new JSONBoolean(b);
    }
    public static JSONable createNumber(Double d) {
        System.out.println("createNumber(" + d + ")");
        return new JSONNumber(d);
    }
    public static JSONable createString(String s) {
        System.out.println("createString(" + s + ")");
        return new JSONString(s);
    }
    public static JSONable createArray() {
        System.out.println("createArray()");
        return new JSONArray();
    }
    public static JSONable createObject() {
        System.out.println("createArray()");
        return new JSONObject();
    }
    private static boolean isSubclass(Object o, Class<?> c) {
        return c.isAssignableFrom(o.getClass());
    }

    /**
     * 
     */
    public static abstract class JSONable {
        public abstract boolean isAcceptable(Class<?> expectClass);
        public abstract <T>T getValue(Class<T> expectClass);
    }
    
    /**
     * 
     */
    public static class JSONNull extends JSONable {
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return false;
        }
        @Override
        public <T> T getValue(Class<T> expectClass) {
            return null;
        }
        public String toString() { return NULL; }
    }
    
    /**
     * 
     */
    public static class JSONBoolean extends JSONable {
        Boolean b;
        public JSONBoolean(Boolean value) {
            this.b = value;
        }
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return expectClass == boolean.class || Boolean.class.isAssignableFrom(expectClass);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getValue(Class<T> expectClass) {
            return (T)b;
        }
        public String toString() {
            if (b == null) {
                return NULL;
            } else {
                return b.toString();
            }
        }
    }
    
    /**
     * 
     */
    public static class JSONNumber extends JSONable {
        private Double number;
        private Class<?> sourceClass = Double.class;

        public static JSONNumber getInstance(Object o) {
            return new JSONNumber((Double)o);
        }
        public JSONNumber(Short value) {
            sourceClass = Short.class;
            this.number = new Double(value);
        }
        public JSONNumber(Integer value) {
            sourceClass = Integer.class;
            this.number = new Double(value);
        }
        public JSONNumber(Long value) {
            sourceClass = Long.class;
            this.number = new Double(value);
        }
        public JSONNumber(Float value) {
            sourceClass = Float.class;
            this.number = new Double(value);
        }
        public JSONNumber(Double value) {
            this.number = value;
        }
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return expectClass == short.class
                    || Short.class.isAssignableFrom(expectClass)
                    || expectClass == int.class
                    || Integer.class.isAssignableFrom(expectClass)
                    || expectClass == long.class
                    || Long.class.isAssignableFrom(expectClass)
                    || expectClass == float.class
                    || Float.class.isAssignableFrom(expectClass)
                    || expectClass == double.class
                    || Double.class.isAssignableFrom(expectClass);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getValue(Class<T> expectClass) {
            if (expectClass == short.class || expectClass == Short.class) {
                return (T)(new Integer(number.intValue()));
            } else if (expectClass == int.class || expectClass == Integer.class) {
                return (T)(new Integer(number.intValue()));
            } else if (expectClass == long.class || expectClass == Long.class) {
                return (T)(new Long(number.longValue()));
            } else if (expectClass == float.class || expectClass == Float.class) {
                return (T)(new Float(number.floatValue()));
            } else {
                return (T)number;
            }
        }
        public String toString() {
            if (number == null) {
                return NULL;
            } else {
                if (sourceClass == Short.class) {
                    return new Short(number.shortValue()).toString();
                } else if (sourceClass == Integer.class) {
                    return new Integer(number.intValue()).toString();
                } else if (sourceClass == Long.class) {
                    return new Long(number.longValue()).toString();
                } else if (sourceClass == Float.class) {
                    return new Float(number.floatValue()).toString();
                } else {
                    return number.toString();
                }
            }
        }
    }
    
    /**
%x22 /          ; "    quotation mark  U+0022
%x5C /          ; \    reverse solidus U+005C
%x2F /          ; /    solidus         U+002F
%x62 /          ; b    backspace       U+0008
%x66 /          ; f    form feed       U+000C
%x6E /          ; n    line feed       U+000A
%x72 /          ; r    carriage return U+000D
%x74 /          ; t    tab             U+0009
%x75 4HEXDIG )  ; uXXXX                U+XXXX
     */
    public static class JSONString extends JSONable {
        private String string;
        public JSONString(String value) {
            this.string = value;
        }
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return String.class.isAssignableFrom(expectClass);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getValue(Class<T> expectClass) {
            return (T)string;
        }
        public String toString() {
            if (string == null) {
                return NULL;
            } else {
                return QUOTE +
                        string
                        .replaceAll("\\\\", "\\\\\\\\")
                        .replaceAll(QUOTE, "\\\\\"")
                        .replaceAll("/", "\\\\/")
                        .replaceAll("\b", "\\\\b")
                        .replaceAll("\f", "\\\\f")
                        .replaceAll("\t", "\\\\t")
                        .replaceAll("\r", "\\\\r")
                        .replaceAll("\n", "\\\\n") +
                        QUOTE;
            }
        }
    }
    
    /**
     * 
     */
    public static class JSONArray extends JSONable {
        private List<JSONable> jsonList = new ArrayList<JSONable>();
        public JSONArray() {
        }
        public JSONArray(boolean[] value) {
            for (boolean b : value) {
                jsonList.add(new JSONBoolean(b));
            }
        }
        public JSONArray(short[] value) {
            for (short s : value) {
                jsonList.add(new JSONNumber(s));
            }
        }
        public JSONArray(int[] value) {
            for (int i : value) {
                jsonList.add(new JSONNumber(i));
            }
        }
        public JSONArray(long[] value) {
            for (long l : value) {
                jsonList.add(new JSONNumber(l));
            }
        }
        public JSONArray(float[] value) {
            for (float f : value) {
                jsonList.add(new JSONNumber(f));
            }
        }
        public JSONArray(double[] value) {
            for (double d : value) {
                jsonList.add(new JSONNumber(d));
            }
        }
        public JSONArray(Object[] value) {
            this(Arrays.asList(value));
        }
        public JSONArray(List<?> valueList) {
            for (Object value : valueList) {
                jsonList.add(jsonify(value));
            }
        }
        public void add(JSONable item) {
            jsonList.add(item);
        }
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return expectClass.isArray() || List.class.isAssignableFrom(expectClass);
        }
        @SuppressWarnings("unchecked")
        @Override
        public <T> T getValue(Class<T> expectClass) {
            if (expectClass.isArray()) {
                int length = jsonList.size();
                Object result = Array.newInstance(expectClass.getComponentType(), length);
                for (int i = 0; i < length; i++) {
                    JSONable json = jsonList.get(i);
                    Array.set(result, i, json.getValue(expectClass.getComponentType())); 
                }
                return (T)result;
            }
            return null;
        }
        public String toString() {
            StringBuffer sb = new StringBuffer(BEGIN_ARRAY);
            String sep = "";
            for (JSONable json : jsonList) {
                sb.append(sep + json.toString());
                sep = VALUE_SEPERATOR;
            }
            sb.append(END_ARRAY);
            return sb.toString();
        }
    }
    
    /**
     * 
     */
    public static class JSONObject extends JSONable {
        private Map<String,JSONable> map = new TreeMap<String,JSONable>();
        
        public JSONObject() {
        }
        public JSONObject(Map<?, ?> value) {
            for (Object key : value.keySet()) {
                map.put(key.toString(), JSON.jsonify(value.get(key)));
            }
        }
        public JSONObject(Object value) {
            if (value == null) {
                return;
            }
            for (Method method : value.getClass().getMethods()) {
                if (!method.getName().startsWith("get")
                        || method.getName().equals("getClass")
                        || method.getReturnType() == null) {
                    continue;
                }
                try {
                    String propertyName = method.getName().substring(3);
                    propertyName =
                            propertyName.substring(0, 1).toLowerCase() +
                            propertyName.substring(1);
                    map.put(propertyName, JSON.jsonify(method.invoke(value)));
                } catch (IllegalAccessException e) {
                    new JSONException(e);
                } catch (IllegalArgumentException e) {
                    new JSONException(e);
                } catch (InvocationTargetException e) {
                    new JSONException(e);
                }
            }
        }
        public void setProperty(String propertyName, JSONable propertyValue) {
            map.put(propertyName, propertyValue);
        }
        @Override
        public boolean isAcceptable(Class<?> expectClass) {
            return true;
        }
        @Override
        public <T> T getValue(Class<T> expectClass) {
            try {
                T result = expectClass.newInstance();
                Map<String,Method> methodMap = new HashMap<String,Method>();
                for (Method method : expectClass.getDeclaredMethods()) {
                    if (!method.getName().startsWith("set") || method.getParameterTypes().length != 1) {
                        continue;
                    }
                    String propertyName = method.getName().substring(3);
                    propertyName =
                            propertyName.substring(0, 1).toLowerCase() +
                            propertyName.substring(1);
                    methodMap.put(propertyName, method);
                }
                for (String key : map.keySet()) {
                    JSONable value = map.get(key);
                    Method method = methodMap.get(key);
                    if (method == null) {
                        throw new JSONException("setter[" + key + "] not found.");
                    }
                    Class<?> paramType = method.getParameterTypes()[0];
                    if (value.isAcceptable(paramType)) {
                        method.invoke(result, value.getValue(paramType));
                    } else {
                        throw new JSONException("paramType[" + paramType + "] is not applicable.");
                    }
                }
                return result;
            } catch (InstantiationException e) {
                throw new JSONException(e);
            } catch (IllegalAccessException e) {
                throw new JSONException(e);
            } catch (IllegalArgumentException e) {
                throw new JSONException(e);
            } catch (InvocationTargetException e) {
                throw new JSONException(e);
            }
        }
        public String toString() {
            StringBuffer sb = new StringBuffer("{");
            String sep = "";
            for (String key : map.keySet()) {
                sb.append(sep);
                sb.append(QUOTE + key + QUOTE + NAME_SEPARATOR);
                sb.append(map.get(key).toString());
                sep = VALUE_SEPERATOR;
            }
            sb.append("}");
            return sb.toString();
        }
    }
    
    /**
     * 
     */
    @SuppressWarnings("serial")
    public static class JSONException extends RuntimeException {
        public JSONException() {
            super();
        }
        public JSONException(Exception e) {
            super(e);
        }
        public JSONException(String msg) {
            super(msg);
        }
    }
}
