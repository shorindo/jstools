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

import java.util.List;

import org.junit.Test;

/**
 * 
 */
public class PaginatorTest {

    @Test
    public void testGetIndicesCase1() {
        Paginator paginator = new Paginator(10);
        List<Integer> result = paginator.getIndices(0);
        assertEquals(1, result.size());
        assertEquals(1, (int)result.get(0));
        
        assertEquals(1, paginator.getIndices(1).size());
        assertEquals(1, paginator.getIndices(2).size());

        paginator = new Paginator(70);
        result = paginator.getIndices(1);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(2, (int)result.get(1));
        assertEquals(3, (int)result.get(2));
        assertEquals(4, (int)result.get(3));
        assertEquals(5, (int)result.get(4));
        assertEquals(6, (int)result.get(5));
        assertEquals(7, (int)result.get(6));
    }

    @Test
    public void testGetIndicesCase2() {
        Paginator paginator = new Paginator(71);
        List<Integer> result = paginator.getIndices(1);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(2, (int)result.get(1));
        assertEquals(3, (int)result.get(2));
        assertEquals(4, (int)result.get(3));
        assertEquals(5, (int)result.get(4));
        assertEquals(-6, (int)result.get(5));
        assertEquals(8, (int)result.get(6));
        
        result = paginator.getIndices(5);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(2, (int)result.get(1));
        assertEquals(3, (int)result.get(2));
        assertEquals(4, (int)result.get(3));
        assertEquals(5, (int)result.get(4));
        assertEquals(-6, (int)result.get(5));
        assertEquals(8, (int)result.get(6));
    }
    
    @Test
    public void testGetIndicesCase3() {
        Paginator paginator = new Paginator(71);
        List<Integer> result = paginator.getIndices(6);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(-3, (int)result.get(1));
        assertEquals(4, (int)result.get(2));
        assertEquals(5, (int)result.get(3));
        assertEquals(6, (int)result.get(4));
        assertEquals(7, (int)result.get(5));
        assertEquals(8, (int)result.get(6));
        
        result = paginator.getIndices(7);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(-3, (int)result.get(1));
        assertEquals(4, (int)result.get(2));
        assertEquals(5, (int)result.get(3));
        assertEquals(6, (int)result.get(4));
        assertEquals(7, (int)result.get(5));
        assertEquals(8, (int)result.get(6));

        result = paginator.getIndices(8);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(-3, (int)result.get(1));
        assertEquals(4, (int)result.get(2));
        assertEquals(5, (int)result.get(3));
        assertEquals(6, (int)result.get(4));
        assertEquals(7, (int)result.get(5));
        assertEquals(8, (int)result.get(6));
    }
    
    @Test
    public void testGetIndicesCase4() {
        Paginator paginator = new Paginator(120);
        List<Integer> result = paginator.getIndices(6);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(-4, (int)result.get(1));
        assertEquals(5, (int)result.get(2));
        assertEquals(6, (int)result.get(3));
        assertEquals(7, (int)result.get(4));
        assertEquals(-8, (int)result.get(5));
        assertEquals(12, (int)result.get(6));
        
        result = paginator.getIndices(3);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(2, (int)result.get(1));
        assertEquals(3, (int)result.get(2));
        assertEquals(4, (int)result.get(3));
        assertEquals(5, (int)result.get(4));
        assertEquals(-6, (int)result.get(5));
        assertEquals(12, (int)result.get(6));
        
        result = paginator.getIndices(9);
        assertEquals(7, result.size());
        assertEquals(1, (int)result.get(0));
        assertEquals(-7, (int)result.get(1));
        assertEquals(8, (int)result.get(2));
        assertEquals(9, (int)result.get(3));
        assertEquals(10, (int)result.get(4));
        assertEquals(11, (int)result.get(5));
        assertEquals(12, (int)result.get(6));
    }
}
