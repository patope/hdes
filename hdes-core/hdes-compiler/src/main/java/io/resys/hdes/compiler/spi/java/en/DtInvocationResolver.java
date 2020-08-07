package io.resys.hdes.compiler.spi.java.en;

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

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.InstanceInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.StaticInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationSpecParams;

public class DtInvocationResolver implements InvocationResolver {

  @Override
  public TypeDef accept(Invocation invocation, AstNodeVisitorContext ctx) {
    try {
      if(invocation instanceof TypeInvocation) {
        return accept((TypeInvocation) invocation, ctx);
      } else if(invocation instanceof StaticInvocation) {
        
        String[] pathName = invocation.getValue().split("\\.");
        DecisionTableBody body = getBody(ctx);
        Optional<TypeDef> typeDef = getTypeDefNode(body.getHeaders().getStatics().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
        
      } else if(invocation instanceof InstanceInvocation) {

        String[] pathName = invocation.getValue().split("\\.");
        DecisionTableBody body = getBody(ctx);
        Optional<TypeDef> typeDef = getTypeDefNode(body.getHeaders().getInstance().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
        
      }
      return accept((MethodInvocation) invocation, ctx);
    } catch(HdesCompilerException e) {
      throw e;
    } catch(Exception e) {
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation), e);
    }
  }
  
  private TypeDef accept(TypeInvocation typeName, AstNodeVisitorContext ctx) {
    String[] pathName = typeName.getValue().split("\\.");
        
    
    if(ctx.getParent().isPresent()) {
      try {
        AstNodeVisitorContext lambdaCtx = ExpressionVisitor.getLambda(ctx).orElse(null);
        if(lambdaCtx != null) {
          
          AstNodeVisitorContext lambdaParent = lambdaCtx.getParent().get();
          MethodInvocation lambdaParentNode = (MethodInvocation) lambdaParent.getValue();
          LambdaExpression expression = (LambdaExpression) lambdaCtx.getValue();
          TypeInvocation lambdaName = expression.getParams().get(0);
          ObjectDef lambdaOn = (ObjectDef) accept(lambdaParentNode.getType().get(), lambdaParent);

          Optional<TypeDef> typeDef = getTypeDefNode(
              ImmutableObjectDef.builder()
              .from(lambdaOn)
              .name(lambdaName.getValue())
              .build(), 
              pathName);
          
          if(typeDef.isPresent()) {
            return typeDef.get();
          }
        }
      } catch (Exception e) {e.printStackTrace();
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName), e);
      }
    }
    
    // Find from inputs
    DecisionTableBody body = getBody(ctx);
    Optional<TypeDef> typeDef = body.getHeaders().getValues().stream()
        .map(f -> getTypeDefNode(f, pathName))
        .filter(f -> f.isPresent())
        .map(f -> f.get()).findFirst();
    
    if(typeDef.isPresent()) {
      return typeDef.get();
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
  }

  private TypeDef accept(MethodInvocation method, AstNodeVisitorContext ctx) {
    final boolean isGlobal = method.getType().isEmpty();
    
    if(isGlobal) {
      Set<ScalarType> typesUsed = new HashSet<>();
      for(AstNode value : method.getValues()) {
        InvocationSpecParams params = ExpressionInvocationSpec.builder().ctx(ctx).build(value);
        if(params.getReturnType() instanceof ObjectDef) {
          ObjectDef objectDef = (ObjectDef) params.getReturnType();
          for(TypeDef typeDef : objectDef.getValues()) {
            if(typeDef instanceof ScalarDef) {
              ScalarDef scalarDef = (ScalarDef) typeDef;
              typesUsed.add(scalarDef.getType());
            }
          }
          
        } else {
          ScalarDef scalarDef = (ScalarDef) params.getReturnType();
          typesUsed.add(scalarDef.getType());
        }
      }
      
      ScalarType scalar = typesUsed.contains(ScalarType.DECIMAL) && 
          typesUsed.contains(ScalarType.INTEGER) ? ScalarType.DECIMAL : typesUsed.iterator().next();
      
      // figure return type
      return ImmutableScalarDef.builder()
          .array(false)
          .required(true)
          .token(method.getToken())
          .name(method.getValue())
          .direction(DirectionType.IN)
          .type(scalar)
          .build();
    }
    
    // Non global methods 
    Invocation typeName = method.getType().get();
    TypeDef typeDef = accept(typeName, ctx);
    
    if(method.getValue().equals("map")) {
      return typeDef;
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getValue()));
  }

  private Optional<TypeDef> getTypeDefNode(TypeDef node, String[] pathName) {
    String path = pathName[0];
    if (!node.getName().equals(path)) {
      return Optional.empty();
    }
    
    if (pathName.length == 1) {
      return Optional.of(node);
    }
    String[] nextPath = Arrays.copyOfRange(pathName, 1, pathName.length);
    
    // Nested structure
    if(node instanceof ObjectDef) {
      ObjectDef objectDefNode = (ObjectDef) node;
      
      for(TypeDef nextNode : objectDefNode.getValues()) {
        Optional<TypeDef> nextResult = getTypeDefNode(nextNode, nextPath);
        if(nextResult.isPresent()) {
          return nextResult;
        }
      }
      
      return Optional.empty();
    
    }
    return Optional.empty();
  }
  
  private static DecisionTableBody getBody(AstNodeVisitorContext ctx) {
    AstNodeVisitorContext parent = ctx;
    do {
      if(parent.getValue() instanceof DecisionTableBody) {
        return (DecisionTableBody) parent.getValue();
      } else {
        parent = parent.getParent().orElse(null);
      }
    } while(parent != null);
    
    return Assertions.fail(() -> "Can't find decision table body for ctx: " + ctx);
  }
}
