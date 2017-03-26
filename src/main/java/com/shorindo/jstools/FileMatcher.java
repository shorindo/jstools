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

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 */
public class FileMatcher {
    private List<Stack<String>> pathList = new ArrayList<Stack<String>>();
    private File base;

    /**
     * 
     */
    public FileMatcher(File base, String patterns) {
        this.base = base;
        if (base == null || !base.exists()) {
            throw new IllegalArgumentException("");
        }
        for (String pattern : patterns.trim().split("[,\\s]+")) {
            Stack<String> pathStack = new Stack<String>();
            String paths[] = pattern.split("/+");
            for (String path : paths) {
                pathStack.push(path);
            }
            pathList.add(pathStack);
        }
    }

    @SuppressWarnings("unchecked")
    public boolean matches(File file) {
        for (Stack<String> pathStack : pathList) {
            Stack<String> p = (Stack<String>)pathStack.clone();
            if (matches(file, p)) {
                return true;
            }
        }
        return false;
    }

    private boolean matches(File file, Stack<String> patternStack) {
        if (patternStack.size() == 0) {
            if (file.equals(base)) {
                return true;
            } else {
                return false;
            }
        }
        String pattern = patternStack.pop();
        if ("**".equals(pattern)) {
            File parent = file;
            while (parent != null) {
                if (matches(parent, patternStack)) {
                    return true;
                }
                parent = parent.getParentFile();
            }
        } else {
            pattern = "^"
                    + pattern.replaceAll("\\.", "\\\\.")
                        .replaceAll("\\?", "\\\\.")
                        .replaceAll("\\*", ".*?")
                    + "$";
            if (Pattern.matches(pattern, file.getName())) {
                return matches(file.getParentFile(), patternStack);
            }
        }
        patternStack.push(pattern);
        return false;
    }

}
