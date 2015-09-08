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

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import com.shorindo.jstools.JSON.JSONObject;
import com.shorindo.jstools.JSON.JSONable;

/**
 * 
 */
public class JsonRpcClient {
    private JsonConnector connector;
    private String version = "2.0";
    private String id;

    public JsonRpcClient(URL url) {
        this(new JsonConnectorImpl(url));
    }
    public JsonRpcClient(JsonConnector connector) {
        this.connector = connector;
        this.connector.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public void setId(String id) {
        this.id = id;
    }
    public <T>T execute(String method, Class<T> resultClass, Object... arg) throws IOException {
        connector.connect();
        JsonRpcRequest request = new JsonRpcRequest();
        request.setJsonrpc(version);
        request.setId(id);
        request.setMethod(method);
        String s = JSON.stringify(request);
        connector.getOutputStream().write(s.getBytes());
        JsonRpcResponse response = new JsonRpcResponse(connector.getInputStream());
        return response.getResult(resultClass);
    }
    protected static String getContent(InputStream is) throws IOException {
        int len;
        char[] c = new char[2048];
        Reader reader = new InputStreamReader(is, "UTF-8");
        StringBuffer sb = new StringBuffer();
        while ((len = reader.read(c)) > 0) {
            sb.append(c, 0, len);
        }
        reader.close();
        return sb.toString();
    }
    private static void debug(Object msg) {
        System.out.println("[D] " + msg);
    }
    private static void warn(Object msg) {
        System.out.println("[W] " + msg);
    }

//    /*
//     * JSType -> undefined null number string array object
//     * Java
//     * null      null      null x      x      x     x
//     * boolean   null      null o      o      x     o
//     * short     null      null o      x      x     x
//     * int       null      null o      x      x     x
//     * long      null      null o      x      x     x
//     * float     null      null o      x      x     x
//     * double    null      null o      x      x     x
//     * String    null      null o      o      x     x
//     * Array     null      null x      x      o     x
//     * List      null      null x      x      o     x
//     * Object    null      null x      x      x     o
//     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
//    protected static <X>X createBeanFromMap(Object o, Class<X> resultClass) {
//        debug("createBeanFromMap(" + o + ", " + resultClass + ")");
//        if (o == null) {
//            return null;
//        } else if (o instanceof Boolean) {
//            return (X)o;
//        } else if (o instanceof Double) {
//            if (resultClass == int.class || resultClass == Integer.class) {
//                return (X)(new Integer(((Double)o).intValue()));
//            } else if (resultClass == long.class || resultClass == Long.class) {
//                return (X)(new Long(((Double)o).longValue()));
//            } else if (resultClass == float.class || resultClass == Float.class) {
//                return (X)(new Float(((Double)o).floatValue()));
//            } else if (resultClass == double.class || resultClass == Double.class) {
//                return (X)((Double)o);
//            } else if (resultClass == String.class) {
//                return (X)(String.valueOf((Double)o));
//            } else {
//                return (X)o;
//            }
//        } else if (o instanceof String) {
//            if (resultClass == int.class || resultClass == Integer.class) {
//                return (X)(String.valueOf(Integer.parseInt((String)o)));
//            } else if (resultClass == long.class || resultClass == Long.class) {
//                return (X)(String.valueOf(Long.parseLong((String)o)));
//            } else if (resultClass == float.class || resultClass == Float.class) {
//                return (X)(String.valueOf(Float.parseFloat((String)o)));
//            } else if (resultClass == double.class || resultClass == Double.class) {
//                return (X)(String.valueOf(Double.parseDouble((String)o)));
//            } else if (resultClass == String.class) {
//                return (X)o;
//            }
//        } else if (o instanceof List) {
//            if (resultClass.isArray()) {
//                List sourceList = (List)o;
//                int size = sourceList.size();
//                X resultArray = (X)Array.newInstance(resultClass.getComponentType(), size);
//                for (int i = 0; i < size; i++) {
//                    Array.set(resultArray, i, (X)createBeanFromMap(sourceList.get(i), resultClass.getComponentType()));
//                }
//                return (X)resultArray;
//            } else if (List.class.isAssignableFrom(resultClass)) {
//                List sourceList = (List)o;
//                List resultList = new ArrayList();
//                Type type = resultClass.getGenericSuperclass();
//                if (type instanceof ParameterizedType) {
//                    Type actualType = ((ParameterizedType)type).getActualTypeArguments()[0];
//                    for (Object item : sourceList) {
//                        resultList.add(createBeanFromMap(item, (Class)actualType));
//                    }
//                    return (X)resultList;
//                } else {
//                    warn("UNKNOWN TYPE:" + o);
//                }
//            } else {
//                warn("UNKNOWN CLASS:" + o);
//            }
//        } else if (o instanceof Map) {
//            try {
//                return setObjectProperty((Map)o, resultClass);
//            } catch (IllegalAccessException e) {
//                e.printStackTrace();
//            } catch (InstantiationException e) {
//                e.printStackTrace();
//            } catch (IllegalArgumentException e) {
//                e.printStackTrace();
//            } catch (InvocationTargetException e) {
//                e.printStackTrace();
//            }
//        } else {
//            return (X)o;
//        }
//        return null;
//    }
//    
//    protected static <X>X setObjectProperty(Map source, Class<X> targetClass) throws InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
//        X target = targetClass.newInstance();
//        Method[] methods = targetClass.getMethods();
//        Map<String,Method> methodMap = new HashMap<String,Method>();
//        for (Method method : methods) {
//            if (!method.getName().matches("^set.*$")) {
//                continue;
//            }
//            String propertyName =
//                    method.getName().replaceAll("^set(.*)$", "$1");
//            propertyName =
//                    propertyName.substring(0, 1).toLowerCase() +
//                    propertyName.substring(1);
//            methodMap.put(propertyName, method);
//        }
//        for (Object key : source.keySet()) {
//            Object value = source.get(key);
//            Method method = methodMap.get(key);
//            if (method == null) {
//                warn("getter method not found for '" + key + "'.");
//                continue;
//            }
//            if (value == null) {
//                target = null;
//            } else if (Boolean.class.isAssignableFrom(value.getClass())) {
//                Class<?> type = method.getParameterTypes()[0];
//                if (type == Boolean.class || type == boolean.class) {
//                    method.invoke(target, value);
//                } else {
//                    warn("getter method not found for '" + key + "'.");
//                }
//            } else if (Double.class.isAssignableFrom(value.getClass())) {
//                Class<?> type = method.getParameterTypes()[0];
//                if (type == Double.class || type == double.class) {
//                    method.invoke(target, value);
//                } else if (type == Integer.class || type == int.class) {
//                    method.invoke(target, ((Double)value).intValue());
//                } else {
//                    warn("getter method not found for '" + key + "'.");
//                }
//            } else if (value.getClass() == String.class) {
//                if (method.getParameterTypes()[0] == String.class) {
//                    method.invoke(target, value);
//                } else {
//                    warn("getter method not found for '" + key + "'.");
//                }
//            } else if (List.class.isAssignableFrom(value.getClass())) {
//                debug("setObjectProperty(" + value.getClass().getSimpleName() + ")");
//            } else if (Map.class.isAssignableFrom(value.getClass())) {
//                Class<?> type = method.getParameterTypes()[0];
//                method.invoke(target, setObjectProperty((Map)value, type));
//            } else {
//                warn("UNKNOWN:" + value.getClass().getSimpleName());
//            }
//        }
//        return target;
//    }

    public static class JsonRpcRequest {
        private String jsonrpc;
        private String method;
        private Object[] params;
        private String id;
        
//        public String toJSON() {
//            ScriptEngineManager manager = new ScriptEngineManager();
//            ScriptEngine engine = manager.getEngineByName("JavaScript");
//            engine.put("request", this);
//            try {
//                return (String)engine.eval(
//                    "(function(o){" +
//                    "    var fn = arguments.callee;" +
//                    "    var result = '{';" +
//                    "    var sep = '';" +
//                    "    for (var key in o) {" +
//                    "        if (typeof o[key] == 'function') continue;" +
//                    "        if (key == 'class') continue;" +
//                    "        /*println(key + ':' + typeof o[key] + '=' + o[key]);*/" +
//                    "        result += sep + '\"' + key + '\":' + fn(o[key]);" +
//                    "        sep = ',';" +
//                    "    }" +
//                    "    result += '}';" +
//                    "    return result;" +
//                    "})(request);"
//                    );
//            } catch (ScriptException e) {
//                throw new RuntimeException(e);
//            }
//        }
        public String getJsonrpc() {
            return jsonrpc;
        }
        public void setJsonrpc(String jsonrpc) {
            this.jsonrpc = jsonrpc;
        }
        public String getMethod() {
            return method;
        }
        public void setMethod(String method) {
            this.method = method;
        }
        public Object[] getParams() {
            return params;
        }
        public void setParams(Object[] params) {
            this.params = params;
        }
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
    }
    public static class JsonRpcResponse {
        private JSONObject response;
        
        public JsonRpcResponse(InputStream is) {
            try {
                response = (JSONObject)JSON.parse(getContent(is));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        public String getJsonrpc() {
            return response.getProperty("jsonrpc", String.class);
        }
        public JsonError getError() {
            return response.getProperty("jsonrpc", JsonError.class);
        }
        public String getId() {
            return response.getProperty("id", String.class);
        }
        public <X>X getResult(Class<X> resultClass) {
            return response.getProperty("result", resultClass);
        }
    }
    protected static class JsonError {
        private int code;
        private String message;
        private Object data;
        public int getCode() {
            return code;
        }
        public void setCode(int code) {
            this.code = code;
        }
        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }
        public Object getData() {
            return data;
        }
        public void setData(Object data) {
            this.data = data;
        }
    }
    public interface JsonConnector {
        public void connect() throws IOException;
        public void setRequestProperty(String key, String value);
        public InputStream getInputStream() throws IOException;
        public OutputStream getOutputStream() throws IOException;
    }

    public static class JsonConnectorImpl implements JsonConnector {
        public static int TIMEOUT = 10000;
        private URL url;
        private URLConnection conn;
        private Map<String,String> props = new HashMap<String,String>();

        public JsonConnectorImpl(URL url) {
            this.url = url;
        }
        @Override
        public void connect() throws IOException {
            URLConnection conn = url.openConnection();
            for (String key : props.keySet()) {
                conn.setRequestProperty(key, props.get(key));
            }
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(TIMEOUT);
        }
        @Override
        public void setRequestProperty(String key, String value) {
            props.put(key, value);
        }
        @Override
        public InputStream getInputStream() throws IOException {
            if (conn == null) {
                throw new IOException("connect first!");
            }
            return conn.getInputStream();
        }
        @Override
        public OutputStream getOutputStream() throws IOException {
            if (conn == null) {
                throw new IOException("connect first!");
            }
            return conn.getOutputStream();
        }
    }
    
    /***************************************************************************
     * 
     */

}
