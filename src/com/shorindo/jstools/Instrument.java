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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.ObjectProperty;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.VariableInitializer;

public abstract class Instrument {
    private List<Pattern> includeList = new ArrayList<Pattern>();
    private List<Pattern> excludeList = new ArrayList<Pattern>();
    protected List<String> fileList;
    protected List<FunctionInfo> functionList = new ArrayList<FunctionInfo>();

    public abstract String instrument(File source) throws IOException;
    
//    public void instrumentSources(File srcDir, File destDir) throws IOException {
//        String sourcePath = srcDir.getAbsolutePath();
//        List<File> sourceList = visit(srcDir);
//        for (File src : sourceList) {
//            String path = src.getAbsolutePath().substring(sourcePath.length());
//            File dest = new File(destDir, path);
//            dest.getParentFile().mkdirs();
//            if (matchPattern(path)) {
//                log("[instrument]" + dest.getAbsolutePath());
//                String instrumented = instrument(src);
//                Writer writer = new FileWriter(dest);
//                writer.write(instrumented);
//                writer.close();
//            } else {
//                if (dest.exists() && dest.lastModified() >= src.lastModified())
//                    continue;
//                log("[copy]" + dest.getAbsolutePath());
//                Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
//            }
//        }
//        generateTools(destDir);
//    }
    
    public void generateTools(File dest) throws IOException {
        File dir = new File(dest, ".jstools");
        dir.mkdirs();
        //
        copyFromResource("template/analyze.css", new File(dir, "analyze.css"));
        copyFromResource("template/analyze.html", new File(dir, "analyze.html"));
        copyFromResource("template/analyze.js", new File(dir, "analyze.js"));
        copyFromResource("template/lib/jquery.js", new File(dir, "lib/jquery.js"));
        copyFromResource("template/lib/kickstart.js", new File(dir, "lib/kickstart.js"));
        copyFromResource("template/lib/w2ui.css", new File(dir, "lib/w2ui.css"));
        copyFromResource("template/lib/w2ui.js", new File(dir, "lib/w2ui.js"));
        
        //
        File mapFile = new File(dir, "function_map.js");
        PrintWriter writer = new PrintWriter(new FileWriter(mapFile));
        writer.println("var functionMap = {");
        writer.println("  'files':[");
        for (String fileName : fileList) {
            writer.print("    '");
            writer.print(fileName);
            writer.print("',");
            writer.println();
        }
        writer.println("  ],");
        writer.println("  'functions':[");
        writer.println("    { id:0, fileId:-1, name:'<root>', row:0, col:0 },");
        for (FunctionInfo info : functionList) {
            writer.print("    { id:");
            writer.print(info.getFunctionId());
            writer.print(", fileId:");
            writer.print(info.getFileId());
            writer.print(", name:'");
            writer.print(info.getName());
            writer.print("', row:");
            writer.print(info.getRow());
            writer.print(", col:");
            writer.print(info.getCol());
            writer.print(" },");
            writer.println("");
        }
        writer.println("  ]");
        writer.println("};\n");
        writer.close();
    }
    
    public void copyFromResource(String resourceName, File dest) throws IOException {
        InputStream is = getClass().getResourceAsStream(resourceName);
        File parent = dest.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        OutputStream os = new FileOutputStream(dest);
        int len = 0;
        byte[] buff = new byte[4096];
        while ((len = is.read(buff)) > 0) {
            os.write(buff, 0, len);
        }
        os.close();
        is.close();
    }
    
    private List<NamedNode> namedList = new ArrayList<NamedNode>();
    public String resolveName(FunctionNode node) {
        NamedNode namedNode = new NamedNode();
        namedNode.setNode(node);
        String name = node.getName();
        if ("".equals(name)) {
            AstNode parent = node.getParent();
            if (parent instanceof Assignment) {
                AstNode left = ((Assignment)parent).getLeft();
                if (left instanceof PropertyGet) {
                    name = left.toSource();
                    //"this.func = .."のときは、上位の関数の名前＋この名前
                    String targetName = ((PropertyGet)left).getTarget().toSource();
                    if ("this".equals(targetName)) {
                        AstNode wrapFunction = parent.getParent();
                        while (wrapFunction != null) {
                            if (wrapFunction instanceof FunctionNode) {
                                name = resolveName((FunctionNode)wrapFunction) + "." + name.replaceAll("^this\\.", "");
                                //インスタンスメソッドはクラスと同じスコープ
                                for (NamedNode target : namedList) {
                                    if (target.getNode() == wrapFunction) {
                                        namedNode.setScope(target.getScope());
                                        break;
                                    }
                                }
                                break;
                            }
                            wrapFunction = wrapFunction.getParent();
                        }
                    }
                } else {
                    name = left.toSource();
                }
            } else if (parent instanceof VariableInitializer) {
                AstNode target = ((VariableInitializer)parent).getTarget();
                name = target.toSource();
            } else if (parent instanceof ObjectProperty) {
                AstNode left = ((ObjectProperty)parent).getLeft();
                if (left instanceof StringLiteral) {
                    name = ((StringLiteral)left).getValue();
                } else {
                    name = left.toSource();
                }
                parent = parent.getParent().getParent();
                while (true) {
                    if (parent instanceof ObjectProperty) {
                        AstNode parentLeft = ((ObjectProperty)parent).getLeft();
                        if (parentLeft instanceof StringLiteral) {
                            name = ((StringLiteral)parentLeft).getValue() + "." + name;
                        } else {
                            name = parentLeft.toSource() + "." + name;
                        }
                        parent = parent.getParent().getParent();
                    } else if (parent instanceof VariableInitializer) {
                        name = ((VariableInitializer)parent).getTarget().toSource() + "." + name;
                        break;
                    } else if (parent instanceof Assignment) {
                        name = ((Assignment)parent).getLeft().toSource() + "." + name;
                        break;
                    } else {
                        break;
                    }
                }
            }
        }
        namedNode.setName(name.replaceAll("\\.prototype", "").replaceAll("^$", "<anonymous>"));
        return namedNode.getName();
    }

    protected List<File> visit(File dir) {
        List<File> result = new ArrayList<File>();
        File files[] = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(visit(file));
            } else {
                result.add(file);
            }
        }
        return result;
    }
    
    protected boolean matchPattern(String path) {
        path = path.replace(File.separatorChar, '/');
        for (Pattern pattern : excludeList) {
            if (pattern.matcher(path).matches()) {
                return false;
            }
        }
        for (Pattern pattern : includeList) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }
    
    protected void includes(String pattern) {
        includeList.add(Pattern.compile("^.*?" + pattern + ".*$"));
    }
    
    protected void excludes(String pattern) {
        excludeList.add(Pattern.compile("^.*?" + pattern + ".*$"));
    }
    
    protected void usage() {
        System.err.println("java " + this.getClass().getName() + " [-p pattern] source destination");
    }
    
    protected void log(String msg) {
        System.out.println(msg);
    }
    
    public class NamedNode {
        private AstNode scope;
        private String name;
        private AstNode node;
        public NamedNode() {
        }
        public NamedNode(AstNode scope, String name, AstNode node) {
            this.scope = scope;
            this.name = name;
            this.node = node;
        }
        public AstNode getScope() {
            return scope;
        }
        public String getName() {
            return name;
        }
        public AstNode getNode() {
            return node;
        }
        public void setScope(AstNode scope) {
            this.scope = scope;
        }
        public void setName(String name) {
            this.name = name;
        }
        public void setNode(AstNode node) {
            this.node = node;
        }
        public int getDepth() {
            int depth = 0;
            AstNode curr = node.getParent();
            while (curr != null && !(curr instanceof AstRoot)) {
                if (curr instanceof FunctionNode) {
                    depth++;
                }
                curr = curr.getParent();
            }
            return depth;
        }
        public String toString() {
            return name;
        }
    }
}
