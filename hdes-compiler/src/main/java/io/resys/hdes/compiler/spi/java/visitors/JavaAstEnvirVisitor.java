package io.resys.hdes.compiler.spi.java.visitors;

import java.io.IOException;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;

/*-
 * #%L
 * hdes-compiler
 * %%
 * Copyright (C) 2020 Copyright 2020 ReSys OÜ
 * %%
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
 * #L%
 */

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.compiler.api.HdesCompiler.Code;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.api.ImmutableCode;

public class JavaAstEnvirVisitor {
  
  public Code visit(AstEnvir envir) {
    
    for(AstNode ast : envir.getValues()) {
      if(ast instanceof DecisionTableBody) {
        visit((DecisionTableBody) ast);
      } else if(ast instanceof FlowBody) {
        visit((FlowBody) ast);
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownAst(ast));
      }
    }
    
    return ImmutableCode.builder().build();
  }
  
  private void visit(DecisionTableBody body) {
    TypeSpec superInterface = visit(new DtAstNodeVisitorJavaInterface().visitDecisionTableBody(body));
    TypeSpec implementation = visit(new DtAstNodeVisitorJavaGen().visitDecisionTableBody(body));
  }
  private void visit(FlowBody body) {
    TypeSpec superInterface = visit(new FlAstNodeVisitorJavaInterface().visitFlowBody(body));
    TypeSpec implementation = visit(new FlAstNodeVisitorJavaGen().visitFlowBody(body));
  }

  private TypeSpec visit(TypeSpec type) {
    try {
      JavaFile file = JavaFile.builder(JavaAstEnvirVisitor.class.getPackage().getName(), type).build();
      file.writeTo(System.out);
      return type;
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }
}
