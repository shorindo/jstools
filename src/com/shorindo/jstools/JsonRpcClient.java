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
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

import com.shorindo.jstools.JSON.JSONObject;
import com.shorindo.jstools.JSON.JSONString;

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

    protected static void debug(Object msg) {
        System.out.println("[D] " + msg);
    }

    protected static void warn(Object msg) {
        System.out.println("[W] " + msg);
    }

    protected static class JsonRpcRequest {
        private JSONObject request;
        private Object params;

        public JsonRpcRequest() {
            request = new JSONObject();
            request.setProperty("jsonrpc", new JSONString("2.0"));
        }
        public String getJsonrpc() {
            return request.getProperty("jsonrpc", String.class);
        }
        public void setJsonrpc(String jsonrpc) {
            request.setProperty("jsonrpc", new JSONString(jsonrpc));
        }
        public String getMethod() {
            return request.getProperty("method", String.class);
        }
        public void setMethod(String method) {
            request.setProperty("method", new JSONString(method));
        }
        public Object getParams() {
            return params;
        }
        public void setParam(Object namedParam) {
            this.params = namedParam;
        }
        public void setParams(Object[] positionParams) {
            this.params = positionParams;
        }
        public String getId() {
            return request.getProperty("id", String.class);
        }
        public void setId(String id) {
            request.setProperty("id", new JSONString(id));
        }
        public String toString() {
            return request.toString();
        }
    }
    protected static class JsonRpcResponse {
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
