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
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.ExpressionStatement;
import org.mozilla.javascript.ast.FunctionCall;
import org.mozilla.javascript.ast.FunctionNode;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.PropertyGet;
import org.mozilla.javascript.ast.ReturnStatement;
import org.mozilla.javascript.ast.StringLiteral;

/**
 * 
 */
public class Profiler extends Instrument {
    private static String OBJECT_NAME = "__jstools__";
    private static String ENTER_NAME = "enter";
    private static String EXIT_NAME = "exit";
    private List<File> fileList;
    private List<FunctionNode> functionNodeList;
    private List<ReturnStatement> returnList;
    private List<FunctionInfo> functionList = new ArrayList<FunctionInfo>();
    private String source;

    public static void main(String args[]) {
        try {
            Profiler profiler = new Profiler();
            profiler.includes(".*\\.js$");
            profiler.instrumentSources(new File("C:/Users/kazm/workspace/XikiEngine/WebContent/html"), new File("dist/instrumented"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public Profiler() {
        fileList = new ArrayList<File>();
        functionNodeList = new ArrayList<FunctionNode>();
        returnList = new ArrayList<ReturnStatement>();
    }

    public String instrument(File source) throws IOException {
        Reader reader = new FileReader(source);
        int len = 0;
        char buf[] = new char[4096];
        StringBuffer sb = new StringBuffer();
        while ((len = reader.read(buf)) > 0) {
            sb.append(buf, 0, len);
        }
        reader.close();
        this.source = sb.toString();

        return _instrument(source, this.source);
    }

    private String _instrument(File file, String source) throws IOException {
        fileList.add(file);
        int fileId = fileList.size();

        AstNode root = parse(new StringReader(source));
        root.visit(new NodeVisitor() {
            @Override
            public boolean visit(AstNode node) {
                if (node instanceof FunctionNode) {
                    functionNodeList.add((FunctionNode)node);
                } else if (node instanceof ReturnStatement) {
                    returnList.add((ReturnStatement)node);
                }
                return true;
            }
        });
        for (FunctionNode node : functionNodeList) {
            insrumentFunction(fileId, node);
        }
        for (ReturnStatement node : returnList) {
            instrumentReturn(node);
        }
        return preProcess(file, fileId) + root.toSource();
    }
    
    public AstRoot parse(Reader reader) throws IOException {
        CompilerEnvirons env = new CompilerEnvirons();
        env.setRecoverFromErrors(true);
        env.setGenerateDebugInfo(true);
        env.setRecordingComments(true);
        env.setRecordingLocalJsDocComments(true);

        IRFactory factory = new IRFactory(env);
        return factory.parse(reader, null, 1);
    }
    
    public String preProcess(File file, int fileId) {
        StringBuffer sb = new StringBuffer();
        try {
            InputStream is = getClass().getResourceAsStream("js/jstools.js");
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            char buf[] = new char[2048];
            int len = 0;
            while ((len = reader.read(buf)) > 0) {
                sb.append(new String(buf, 0, len));
            }
            reader.close();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return sb.toString();
    }
    
    protected void insrumentFunction(int fileId, FunctionNode node) {
        Name objName = new Name();
        objName.setIdentifier(OBJECT_NAME);
        Name callName = new Name();
        callName.setIdentifier(ENTER_NAME);
        PropertyGet propertyGet = new PropertyGet();
        propertyGet.setTarget(objName);
        propertyGet.setProperty(callName);
        FunctionCall call = new FunctionCall();
        FunctionInfo info = new FunctionInfo();
        info.setFileId(fileId);
        info.setFunctionId(functionList.size());
        info.setName(resolveName(node));
        info.setRow(node.getLineno());
        info.setCol(getColumn(node));
        functionList.add(info);
        call.setTarget(propertyGet);
        NumberLiteral functionId = new NumberLiteral();
        functionId.setValue(String.valueOf(functionList.size()));
        call.addArgument(functionId);
        ExpressionStatement stmt = new ExpressionStatement();
        stmt.setExpression(call);
//      NumberLiteral functionId = new NumberLiteral();
//      functionId.setValue(String.valueOf(functionList.size()));
//      call.addArgument(functionId);
//        NumberLiteral idLiteral = new NumberLiteral();
//        idLiteral.setValue(String.valueOf(fileId));
//        call.addArgument(idLiteral);
//        StringLiteral nameLiteral = new StringLiteral();
//        nameLiteral.setQuoteCharacter('\'');
//        nameLiteral.setValue(resolveName(node));
//        call.addArgument(nameLiteral);
//      ExpressionStatement stmt = new ExpressionStatement();
//      stmt.setExpression(call);
//        NumberLiteral line = new NumberLiteral();
//        line.setValue(String.valueOf(node.getLineno()));
//        call.addArgument(line);
//        NumberLiteral column = new NumberLiteral();
//        column.setValue(String.valueOf(getColumn(node)));
//        call.addArgument(column);

        AstNode body = node.getBody();
        Node first = body.getFirstChild();
        if (first != null) {
            body.addChildBefore(stmt, first);
        } else {
            body.addChild(stmt);
        }
        Node last = body.getLastChild();
        if (!(last instanceof ReturnStatement)) {
            ReturnStatement returnNode = new ReturnStatement();
            instrumentReturn(returnNode);
            body.addChild(returnNode);
        }
    }
    
    private int getColumn(AstNode node) {
        if (this.source == null || this.source.length() < node.getAbsolutePosition())
            return 0;
        String forward = this.source.substring(0, node.getAbsolutePosition());
        int pos = forward.lastIndexOf('\n');
        if (pos < 0) {
            pos = forward.length() + 1;
        } else {
            pos = forward.length() - pos;
        }
        return pos;
    }
    
    protected void instrumentReturn(ReturnStatement node) {
        Name exitName = new Name();
        exitName.setIdentifier(EXIT_NAME);
        PropertyGet propertyGet = new PropertyGet();
        Name tracerName = new Name();
        tracerName.setIdentifier(OBJECT_NAME);
        propertyGet.setTarget(tracerName);
        propertyGet.setProperty(exitName);
        FunctionCall call = new FunctionCall();
        call.setTarget(propertyGet);

        AstNode returnValue = node.getReturnValue();
        if (returnValue != null) {
            node.removeChildren();
            call.addArgument(returnValue);
        }
        node.setReturnValue(call);
    }

}
