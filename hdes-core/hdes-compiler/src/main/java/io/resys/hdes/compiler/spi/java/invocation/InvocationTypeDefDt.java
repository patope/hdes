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

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
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
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.invocation.InvocationSpec.InvocationSpecParams;

public class InvocationTypeDefDt implements InvocationTypeDef {
  private final AstEnvir envir;
  
  public InvocationTypeDefDt(AstEnvir envir) {
    super();
    this.envir = envir;
  }

  @Override
  public TypeDef getTypeDef(Invocation invocation, AstNodeVisitorContext ctx) {
    try {
      if(invocation instanceof TypeInvocation) {
        return accept((TypeInvocation) invocation, ctx);
      } else if(invocation instanceof StaticInvocation) {
        
        String[] pathName = invocation.getValue().split("\\.");
        Body body = TypeDefFinder.getBody(ctx);
        Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body.getHeaders().getStatics().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(invocation));
        
      } else if(invocation instanceof InstanceInvocation) {

        String[] pathName = invocation.getValue().split("\\.");
        Body body = TypeDefFinder.getBody(ctx);
        Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body.getHeaders().getInstance().get(), pathName);
        if(typeDef.isPresent()) {
          return typeDef.get();
        }
        
        for(TypeDef childTypeDef : body.getHeaders().getInstance().get().getValues()) {
          Optional<TypeDef> instanceTypeDef = TypeDefFinder.getTypeDef(childTypeDef, pathName);
          if(instanceTypeDef.isPresent()) {
            return instanceTypeDef.get();
          }
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
        
    AstNodeVisitorContext lambdaCtx = TypeDefFinder.getLambda(ctx).orElse(null);
    if(lambdaCtx != null) {
      AstNodeVisitorContext lambdaParent = lambdaCtx.getParent().get();
      MethodInvocation lambdaParentNode = (MethodInvocation) lambdaParent.getValue();
      LambdaExpression expression = (LambdaExpression) lambdaCtx.getValue();
      TypeInvocation lambdaName = expression.getParams().get(0);
      ObjectDef lambdaOn = (ObjectDef) getTypeDef(lambdaParentNode.getType().get(), lambdaParent);

      Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(
          ImmutableObjectDef.builder()
          .from(lambdaOn)
          .name(lambdaName.getValue())
          .build(), 
          pathName);
      
      if(typeDef.isPresent()) {
        return typeDef.get();
      }
    }
    
    // Find from inputs
    Body body = TypeDefFinder.getBody(ctx);
    Optional<TypeDef> typeDef = TypeDefFinder.getTypeDef(body, pathName);
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
        InvocationSpecParams params = InvocationSpec.builder().envir(envir).ctx(ctx).build(value);
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
    TypeDef typeDef = getTypeDef(typeName, ctx);
    
    if(method.getValue().equals("map")) {
      return typeDef;
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getValue()));
  }

  @Override
  public CodeBlock getMethod(Invocation invocation, AstNodeVisitorContext ctx) {
    TypeDef typeDef = getTypeDef(invocation, ctx);
    return InvocationGetMethod.builder().ctx(ctx).typeDef(typeDef).invocation(invocation).build();
  }
}
