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

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.InstanceInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.StaticInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;

public class InvocationGetMethodDt {
  public final static String ACCESS_OUTPUT_VALUE = "outputValue";
  public final static String ACCESS_STATIC_VALUE = "staticValue";
  public final static String ACCESS_STATIC_VALUES = "values";
  public final static String ACCESS_INSTANCE_VALUE = "instanceValue";
  
  
  public CodeBlock apply(
      AstNodeVisitorContext ctx, 
      Invocation node, 
      TypeDef typeDef) {
    
    String typeName = node.getValue();
    Optional<String> lambdaContext = TypeDefFinder.getLambda(ctx).map(runningCtx -> {
      LambdaExpression lambda = (LambdaExpression) runningCtx.getValue();
      return lambda.getParams().get(0).getValue();
    });

    CodeBlock.Builder value = CodeBlock.builder();
    if(lambdaContext.isPresent() && 
        (typeName.equals(lambdaContext.get()) || typeName.startsWith(lambdaContext.get() + ".")) && 
        node instanceof TypeInvocation) {
      
      String name = JavaSpecUtil.methodVarCall(node.getValue());
      value.add(name + (typeDef.getRequired() ? "" : ".get()"));
      
    } else if(node instanceof StaticInvocation) {
    
      String name = typeName.replaceFirst("static", ACCESS_STATIC_VALUE);
      value.add("$L.$L", ExpressionVisitor.ACCESS_SRC_VALUE, JavaSpecUtil.methodCall(name) + (typeDef.getRequired() ? "" : ".get()"));
      
    } else if(node instanceof InstanceInvocation) {
      String name = JavaSpecUtil.methodCall(ACCESS_OUTPUT_VALUE);
      value.add("$L.$L", ExpressionVisitor.ACCESS_SRC_VALUE, name + (typeDef.getRequired() ? "" : ".get()"));
    
    } else if(typeDef.getDirection() == DirectionType.IN) {
      String name = JavaSpecUtil.methodCall(InvocationGetMethod.ACCESS_INPUT_VALUE + "." + typeName);
      value.add("$L.$L", ExpressionVisitor.ACCESS_SRC_VALUE, name + (typeDef.getRequired() ? "" : ".get()"));
    
    } else {
      String name = JavaSpecUtil.methodCall(ACCESS_OUTPUT_VALUE + "." + typeName);
      value.add("$L.$L", ExpressionVisitor.ACCESS_SRC_VALUE, name + (typeDef.getRequired() ? "" : ".get()"));
    }
    return value.build();
  }
}
