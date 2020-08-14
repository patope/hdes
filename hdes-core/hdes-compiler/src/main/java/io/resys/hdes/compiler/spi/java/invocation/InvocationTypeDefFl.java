package io.resys.hdes.compiler.spi.java.invocation;

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

import java.util.Optional;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.TaskRef;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ImmutableInstanceInvocation;
import io.resys.hdes.compiler.api.HdesCompilerException;

public class InvocationTypeDefFl implements InvocationTypeDef {

  private final AstEnvir envir;
  
  public InvocationTypeDefFl(AstEnvir envir) {
    super();
    this.envir = envir;
  }

  @Override
  public TypeDef getTypeDef(Invocation invocation, AstNodeVisitorContext ctx) {
    try {
      FlowBody flow = TypeDefFinder.getBody(ctx);
      if(invocation instanceof MethodInvocation) {
        MethodInvocation method = (MethodInvocation) invocation;
        if(method.getType().isPresent()) {
          Invocation delegate = method.getType().get();
          String[] pathName = delegate.getValue().split("\\.");
          Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), pathName[0]); 
          
          if(task.isPresent() && !task.get().getRef().isEmpty()) {
            TaskRef taskRef = task.get().getRef().get();
            return InvocationTypeDef.builder()
                .envir(envir).body(envir.getBody(taskRef.getValue())).build()
                .getTypeDef(deconstruct(delegate), ImmutableAstNodeVisitorContext.builder().parent(ctx).value(delegate).build());
          }
        }
        
        return new InvocationTypeDefDt(envir).getTypeDef(invocation, ctx);
      }
      
      
      String[] pathName = invocation.getValue().split("\\.");
      Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), pathName[0]);
      
      
      // Get delegate output
      if(task.isPresent() && !task.get().getRef().isEmpty()) {
        TaskRef taskRef = task.get().getRef().get();
        Body delegate = envir.getBody(taskRef.getValue());
        return InvocationTypeDef.builder()
            .envir(envir).body(delegate).build()
            .getTypeDef(deconstruct(invocation), ImmutableAstNodeVisitorContext.builder().parent(ctx).value(delegate).build());
      }
      
      Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(flow, pathName);
      if(typeDef.isPresent()) {
        return typeDef.get();
      }

      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
    } catch(HdesCompilerException e) {
      throw e;
    } catch(Exception e) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation), e);
    }
  }
  
  private Invocation deconstruct(Invocation src) {
    String nextPath = "instance." + src.getValue().substring(src.getValue().indexOf(".") + 1);
    return ImmutableInstanceInvocation.builder().from(src).value(nextPath).build();
  }

  @Override
  public CodeBlock getMethod(Invocation invocation, AstNodeVisitorContext ctx) {
    TypeDef typeDef = getTypeDef(invocation, ctx);
    return InvocationGetMethod.builder().ctx(ctx).typeDef(typeDef).invocation(invocation).build();
  }
}
