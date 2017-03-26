/*
 * Copyright 2017 Shorindo, Inc.
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
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * 
 */
public class FileMatcherTest {
    private static File base = new File(".");

    @Test
    public void test1() {
        FileMatcher fileMatcher = new FileMatcher(base, "path/to/*.txt");
        assertTrue(fileMatcher.matches(new File(base, "path/to/file.txt")));
        assertFalse(fileMatcher.matches(new File(base, "path/to/file.tx")));
    }

    @Test
    public void test2() {
        FileMatcher fileMatcher = new FileMatcher(base, "**/*.txt");
        assertTrue(fileMatcher.matches(new File(base, "path/to/file.txt")));
    }

    @Test
    public void test3() {
        FileMatcher fileMatcher = new FileMatcher(base, "path/*/*.txt");
        assertTrue(fileMatcher.matches(new File(base, "path/to/file.txt")));
    }

    @Test
    public void test4() {
        FileMatcher fileMatcher = new FileMatcher(base, "path/**/file.*");
        assertTrue(fileMatcher.matches(new File(base, "path/to/file.txt")));
    }

    @Test
    public void test5() {
        FileMatcher fileMatcher = new FileMatcher(base, "**");
        assertTrue(fileMatcher.matches(new File(base, "path/to/file.txt")));
    }

    @Test
    public void test6() {
        FileMatcher fileMatcher = new FileMatcher(base, "*");
        assertFalse(fileMatcher.matches(new File(base, "path/to/file.txt")));
    }
}
