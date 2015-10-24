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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.shorindo.jstools.JsonRpcClient.JsonConnector;
import com.shorindo.jstools.JsonRpcClient.JsonRpcRequest;

/**
 * 
 */
public class JsonRpcClientTest {
    
    protected void assertResponse(Class<?> expectClass, Object expectValue, final String response) throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return response;
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        assertEquals(expectValue, client.execute("method", expectClass));
    }

    @Test
    public void testReturnBoolean() throws Exception {
        assertResponse(boolean.class, true, "{\"jsonrpc\":\"2.0\",\"result\":true}");
        assertResponse(boolean.class, false, "{\"jsonrpc\":\"2.0\",\"result\":false}");
    }
    @Test
    public void testReturnString() throws Exception {
        assertResponse(String.class, "abcd", "{\"jsonrpc\":\"2.0\",\"result\":\"abcd\"}");
        assertResponse(String.class, null, "{\"jsonrpc\":\"2.0\",\"result\":null}");
    }
    @Test
    public void testReturnInt() throws Exception {
        assertResponse(int.class, 1234, "{\"jsonrpc\":\"2.0\",\"result\":1234}");
        assertResponse(Integer.class, 1234, "{\"jsonrpc\":\"2.0\",\"result\":1234}");
        assertResponse(int.class, 1234, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(int.class, null, "{\"jsonrpc\":\"2.0\",\"result\":null}");
    }
    @Test
    public void testReturnLong() throws Exception {
        assertResponse(long.class, (long)1234, "{\"jsonrpc\":\"2.0\",\"result\":1234}");
        assertResponse(Long.class, (long)1234, "{\"jsonrpc\":\"2.0\",\"result\":1234}");
        assertResponse(long.class, (long)1234, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(long.class, null, "{\"jsonrpc\":\"2.0\",\"result\":null}");
    }
    @Test
    public void testReturnFloat() throws Exception {
        assertResponse(float.class, (float)1234, "{\"jsonrpc\":\"2.0\",\"result\":1234}");
        assertResponse(Float.class, (float)1234.5678, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(float.class, (float)1234.5678, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(float.class, null, "{\"jsonrpc\":\"2.0\",\"result\":null}");
    }
    @Test
    public void testReturnDouble() throws Exception {
        assertResponse(double.class, (double)1234.5678, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(Double.class, (double)1234.5678, "{\"jsonrpc\":\"2.0\",\"result\":1234.5678}");
        assertResponse(double.class, null, "{\"jsonrpc\":\"2.0\",\"result\":null}");
    }
    @Test
    public void testReturnObject() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":{}}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        TestClass result = client.execute("method", TestClass.class);
        assertNotNull(result);
        
        stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":{\"i\":123,\"d\":123.456,\"s\":\"hoge\",\"o\":{\"i\":234}}}";
            }
        };
        client = new JsonRpcClient(stub);
        result = client.execute("method", TestClass.class);
        assertNotNull(result);
        assertEquals((int)123, result.getI());
        assertEquals((double)123.456, result.getD(), 0.0001);
        assertEquals("hoge", result.getS());
        assertEquals((int)234, result.getO().getI());
    }
    @Test
    public void testReturnIntArray() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[1,2,3]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        int[] result = client.execute("method", int[].class);
        assertEquals(1, result[0]);
        assertEquals(2, result[1]);
        assertEquals(3, result[2]);
    }
    @SuppressWarnings({ "serial", "unchecked" })
    @Test
    public void testReturnIntList() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[1,2,3]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        List<Integer> r = new ArrayList<Integer>(){};
        List<Integer> result = client.execute("method", r.getClass());
        assertEquals((Integer)1, result.get(0));
        assertEquals((Integer)2, result.get(1));
        assertEquals((Integer)3, result.get(2));
        
        List<?> x = client.execute("method", List.class);
        assertEquals(1.0, result.get(0), 0.01);
        assertEquals(2.0, result.get(1), 0.01);
        assertEquals(3.0, result.get(2), 0.01);
    }
    @Test
    public void testReturnStringArray() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[\"a\",\"b\",\"c\"]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        String[] result = client.execute("method", String[].class);
        assertEquals("a", result[0]);
        assertEquals("b", result[1]);
        assertEquals("c", result[2]);
    }
    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testReturnStringList() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[\"a\",\"b\",\"c\"]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        List<String> type = new ArrayList<String>(){};
        List<String> result = client.execute("method", type.getClass());
        assertEquals("a", result.get(0));
        assertEquals("b", result.get(1));
        assertEquals("c", result.get(2));
    }
    @Test
    public void testReturnObjectArray() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[{\"i\":123},{\"d\":123.456},{\"s\":\"abc\"}]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        TestClass[] result = client.execute("method", TestClass[].class);
        assertEquals((int)123, result[0].i);
        assertEquals((double)123.456, result[1].d, 0.0001);
        assertEquals("abc", result[2].s);
    }
    @SuppressWarnings({ "unchecked", "serial" })
    @Test
    public void testReturnObjectList() throws Exception {
        URL url = new URL("http://localhost:8080/wiki");
        JsonConnectorStub stub = new JsonConnectorStub(url) {
            protected String getResponse() {
                return "\n{\"jsonrpc\":\"2.0\",\"result\":[{\"i\":123},{\"d\":123.456},{\"s\":\"abc\"}]}";
            }
        };
        JsonRpcClient client = new JsonRpcClient(stub);
        List<TestClass> type = new ArrayList<TestClass>(){};
        List<TestClass> result = client.execute("method", type.getClass());
        assertEquals((int)123, result.get(0).i);
        assertEquals((double)123.456, result.get(1).d, 0.0001);
        assertEquals("abc", result.get(2).s);
    }

    /***************************************************************************
     * JsonRpcRequest Test
     */
    @Test
    public void testJsonRpcRequest() throws IOException {
        JsonRpcRequest request = new JsonRpcRequest();
        request.setId("1234");
        request.setJsonrpc("2.0");
        request.setMethod("method");
        request.setParams(new String[] { "a", "b", "c" });
        //System.out.println(request.toString());
    }
    
    /***************************************************************************
     * 
     */
    public static class JsonConnectorStub implements JsonConnector {
        private URL url;
        private Map<String,String> props = new HashMap<String,String>();
        private ByteArrayOutputStream bos = new ByteArrayOutputStream();

        public JsonConnectorStub(URL url) {
            this.url = url;
        }
        protected String getRequest() {
            return new String(bos.toByteArray());
        }
        protected String getResponse() {
            return "{\"jsonrpc\":\"2.0\",\"result\":[]}";
        }
        @Override
        public void connect() throws IOException {
            bos.write(("POST " + url.toString() + " HTTP/1.1").getBytes());
            for (String key : props.keySet()) {
                bos.write((key + ": " + props.get(key) + "\n").getBytes());
            }
            bos.write("\n".getBytes());
        }
        @Override
        public void setRequestProperty(String key, String value) {
            props.put(key, value);
        }
        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(getResponse().getBytes());
        }
        @Override
        public OutputStream getOutputStream() throws IOException {
            return bos;
            //return System.out;
        }
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
}
