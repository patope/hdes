package io.resys.hdes.compiler.spi.invocation;

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

import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingExpression;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;

public class InvocationGetMethodFl {
  
  public CodeBlock apply(
      AstNodeVisitorContext ctx, 
      Invocation node, 
      TypeDef typeDef) {
    
    final FlowBody flow = TypeDefFinder.getBody(ctx);
    final String typeName = node.getValue();
    final Optional<String> lambdaContext = TypeDefFinder.getLambda(ctx).map(runningCtx -> {
      LambdaExpression lambda = (LambdaExpression) runningCtx.getValue();
      return lambda.getParams().get(0).getValue();
    });

    if(lambdaContext.isPresent() && 
        (typeName.equals(lambdaContext.get()) || typeName.startsWith(lambdaContext.get() + ".")) && 
        node instanceof TypeInvocation) {
      
      String name = JavaSpecUtil.methodVarCall(node.getValue());
      return CodeBlock.builder().add(name + (typeDef.getRequired() ? "" : ".get()")).build();
      
    }
    final Optional<MappingExpression> mappingExpression = TypeDefFinder.getNode(MappingExpression.class, ctx);
    
    // getFlow input or task output
    String[] pathName = node.getValue().split("\\.");
    String taskName = pathName[0];
    Optional<FlowTaskNode> task = TypeDefFinder.getTask(flow.getTask(), taskName);
    if(task.isPresent()) {
      String nextPath = node.getValue().substring(node.getValue().indexOf(".") + 1);
      return CodeBlock.builder()
        .add("after.$L.getDelegate().getOutputValue().$L", 
            JavaSpecUtil.methodCall(taskName),
            JavaSpecUtil.methodCall(nextPath))
        .build();
    }
    
    final String required = (typeDef.getRequired() ? "" : ".get()");
    if(mappingExpression.isPresent()) {
      return CodeBlock.builder().add("input.$L", JavaSpecUtil.methodCall(node.getValue()) + required).build();      
    }
    String name = JavaSpecUtil.methodCall(InvocationGetMethod.ACCESS_INPUT_VALUE + "." + node.getValue());
    return CodeBlock.builder().add("input.$L", name + required).build();
  }
}
