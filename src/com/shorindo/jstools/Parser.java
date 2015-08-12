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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.mozilla.javascript.CompilerEnvirons;
import org.mozilla.javascript.IRFactory;
import org.mozilla.javascript.Node;
import org.mozilla.javascript.ast.Assignment;
import org.mozilla.javascript.ast.AstNode;
import org.mozilla.javascript.ast.AstRoot;
import org.mozilla.javascript.ast.InfixExpression;
import org.mozilla.javascript.ast.KeywordLiteral;
import org.mozilla.javascript.ast.Name;
import org.mozilla.javascript.ast.NodeVisitor;
import org.mozilla.javascript.ast.NumberLiteral;
import org.mozilla.javascript.ast.StringLiteral;
import org.mozilla.javascript.ast.UnaryExpression;

public class Parser {

    public Parser() {
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

    protected String fixDecimal(NumberLiteral node) {
        String literal = node.toSource();
        if (literal.matches("^[\\+\\-]?[0-7]+$")) {
            if (Long.parseLong(literal, 10) == node.getDouble()) {
                return literal;
            } else if (Long.parseLong(literal, 8) == node.getDouble()) {
                return "0" + literal;
            } else if (Long.parseLong(literal, 16) == node.getDouble()) {
                return "0x" + literal;
            }
        } else if (literal.matches("^[\\+\\-]?[0-9a-fA-F]+$")) {
            if (literal.matches("^[0-9]+$") && Long.parseLong(literal, 10) == node.getDouble()) {
                return literal;
            } else if (Long.parseLong(literal, 16) == node.getDouble()) {
                return "0x" + literal;
            }
        }
        return literal;
    }

    public String makeTree(AstNode node) throws FileNotFoundException, IOException {
        final StringBuffer sb = new StringBuffer();
        //final int depthOffset = node instanceof AstRoot ? -1 : -node.depth();
        if (node instanceof AstRoot) {
            Class<?> clazz = node.getClass();
            sb.append(node.makeIndent(node.depth()));
            sb.append(clazz.getSimpleName());
            for (Iterator<Node> it = node.iterator(); it.hasNext();) {
                sb.append(makeTree((AstNode)it.next()));
            }
        } else {
            node.visit(new NodeVisitor() {
                @Override
                public boolean visit(AstNode node) {
                    Class<?> clazz = node.getClass();
                    sb.append(node.makeIndent(node.depth()));
                    sb.append(clazz.getSimpleName());
                    if (clazz == KeywordLiteral.class ||
                        clazz == Name.class ||
                        clazz == StringLiteral.class) {
                        sb.append(" : " + node.toSource());
                    } else if (clazz == NumberLiteral.class) {
                        //avoid rhino's bug
                        sb.append(" : " + fixDecimal((NumberLiteral)node));
                    } else if (clazz == UnaryExpression.class) {
                        sb.append(" : " + AstNode.operatorToString(node.getType()));
                    } else if (clazz == InfixExpression.class) {
                        sb.append(" : " + AstNode.operatorToString(node.getType()));
                    } else if (clazz == Assignment.class) {
                        sb.append(" : " + AstNode.operatorToString(node.getType()));
                    }
                    sb.append("\n");
                    return true;
                }
            });
        }

        return sb.toString();
    }
}
