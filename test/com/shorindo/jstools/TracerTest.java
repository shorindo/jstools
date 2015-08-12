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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringReader;
import java.io.Writer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.ast.AstRoot;

/**
 * 
 */
public class TracerTest {
    Tracer tracer;
    File file;
    
    @Before
    public void setUp() throws Exception {
        tracer = new Tracer();
        file = File.createTempFile("test", ".js");
    }
    
    @After
    public void tearDown() throws Exception {
        file.delete();
    }

    /**
     * check rhino's bug
     */
    @Test
    public void testDecimal() throws IOException {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(true);
        env.setGenerateDebugInfo(true);
        env.setRecordingComments(true);
        env.setRecordingLocalJsDocComments(true);

        IRFactory factory = new IRFactory(env);
        StringReader reader = new StringReader("var x = 0x01;\n");
        AstRoot root = factory.parse(reader, null, 1);
        assertEquals("var x = 0x01;\n", root.toSource());

        factory = new IRFactory(env);
        reader = new StringReader("var x = 001;\n");
        root = factory.parse(reader, null, 1);
        assertEquals("var x = 001;\n", root.toSource());
    }

    /**
     * 
     */
    @Test
    public void testFunction1() throws Exception {
        createSource("function x() {}");
        String result = tracer.instrument(file);
        assertMatch("__tracer__.enter(1, 1, 'x', 1, 1);", result);
    }
    
    @Test
    public void testFunction2() throws Exception {
        createSource("var x = function() {}");
        String result = tracer.instrument(file);
        assertMatch("__tracer__.enter(1, 1, 'x', 1, 9);", result);
    }

    @Test
    public void testFunction3() throws Exception {
        createSource("var a = 123;\nfunction xyz() {}");
        String result = tracer.instrument(file);
        assertMatch("__tracer__.enter(1, 1, 'xyz', 2, 1);", result);
    }

    @Test
    public void testFunction4() throws Exception {
        createSource("var a = 1; function xyz() {}");
        String result = tracer.instrument(file);
        assertMatch("__tracer__.enter(1, 1, 'xyz', 1, 12);", result);
    }

    @Test
    public void testFunction5() throws Exception {
        createSource("(function() {})();");
        String result = tracer.instrument(file);
        assertMatch("__tracer__.enter(1, 1, '', 1, 2);", result);
    }
    
    private void assertMatch(String expect, String actual) {
        if (actual.contains(expect)) {
            assertTrue(actual, true);
        } else {
            fail(actual);
        }
    }

    private void createSource(String source) throws IOException {
        Writer writer = new FileWriter(file);
        writer.write(source);
        writer.close();
    }
}
