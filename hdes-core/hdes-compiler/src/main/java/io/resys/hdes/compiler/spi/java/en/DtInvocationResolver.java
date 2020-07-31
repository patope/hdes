package io.resys.hdes.compiler.spi.java.en;

import java.util.ArrayList;

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
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.Token;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationSpecParams;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableStaticValue;

public class DtInvocationResolver implements InvocationResolver {

  @Override
  public TypeDef accept(Invocation invocation, AstNodeVisitorContext ctx) {
    if(invocation instanceof TypeInvocation) {
      return accept((TypeInvocation) invocation, ctx);
    }
    return accept((MethodInvocation) invocation, ctx);
  }
  
  private TypeDef accept(TypeInvocation typeName, AstNodeVisitorContext ctx) {
    String[] pathName = typeName.getValue().split("\\.");
        
    // static data access
    if(typeName.getScope() == TypeNameScope.STATIC) {
      ObjectDef staticValue = ImmutableObjectDef.builder()
          .array(false)
          .required(true)
          .direction(DirectionType.IN)
          .addAllValues(getStaticValues(ctx))
          .name(typeName.getValue())
          .token(typeName.getToken())
          .build();
      
      Optional<TypeDef> typeDef = getTypeDefNode(staticValue, pathName);
      if(typeDef.isPresent()) {
        return typeDef.get();
      }
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
    }
    
    if(ctx.getParent().isPresent()) {
      try {
        AstNodeVisitorContext parentCtx = ctx.getParent().get();
        AstNode parentNode = parentCtx.getValue();
        if(parentNode instanceof LambdaExpression && parentCtx.getParent().isPresent()) {
          AstNodeVisitorContext lambdaParent = parentCtx.getParent().get();
          MethodInvocation lambdaParentNode = (MethodInvocation) lambdaParent.getValue();
          LambdaExpression expression = (LambdaExpression) parentNode;
          TypeInvocation lambdaName = expression.getParams().get(0);
          ObjectDef lambdaOn = (ObjectDef) accept(lambdaParentNode.getType().get(), lambdaParent);

          Optional<TypeDef> typeDef = getTypeDefNode(
              ImmutableObjectDef.builder()
              .from((ObjectDef) lambdaOn.getValues().get(0))
              .name(lambdaName.getValue())
              .build(), 
              pathName);
          
          if(typeDef.isPresent()) {
            return typeDef.get();
          }
        }
      } catch (Exception e) {
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
        AstNodeVisitorContext childCtx = ImmutableAstNodeVisitorContext.builder().parent(ctx).value(value).build(); 
        
        if(value instanceof ExpressionBody) {
          //ExpressionRefsSpec.builder(this).body(value);        
          InvocationSpecParams params = ExpressionInvocationSpec.builder().ctx(childCtx).build((ExpressionBody) value);
          
          
        } else if(value instanceof MethodInvocation) {
          
          TypeDef child = accept((MethodInvocation) value, childCtx);
          System.out.println(value);
        } else if(value instanceof TypeDef) {
          
          TypeDef child = (TypeDef) value;
          
          System.out.println(value);
          
        } else {
          System.out.println(value);
        }

      }
      
      // figure return type
      return ImmutableScalarDef.builder()
          .array(false)
          .required(true)
          .token(method.getToken())
          .name(method.getName())
          .direction(DirectionType.IN)
          .type(ScalarType.BOOLEAN)
          .build();
      
      /*
      return ImmutableObjectDef.builder()
        .array(true)
        .required(false)
        .values(body.getHeaders().getValues().stream()
            .filter(h -> h.getDirection() == DirectionType.OUT)
            .collect(Collectors.toList()))
        .name(name)
        .token(method.getToken())
        .build();*/
    }
    

    
    // Non global methods 
    TypeInvocation typeName = method.getType().get();
    TypeDef typeDef = accept(typeName, ctx);
    
    if(method.getName().equals("map")) {
      Optional<TypeDef> result = getTypeDefNode(typeDef, new String[] {"static", ExpressionVisitor.ACCESS_STATIC_VALUES});
      if(result.isPresent()) {
        return result.get();
      }
      throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getName()));
    }
    
    
    // type + lambda expression
    if( method.getValues().size() == 1 && 
        method.getName().equals("map") &&
        method.getValues().get(0) instanceof LambdaExpression) {
      
      LambdaExpression lambda = (LambdaExpression) method.getValues().get(0);
      InvocationSpecParams lambdaSpec = ExpressionInvocationSpec.builder().ctx(ctx).build(lambda);
      System.out.println(lambdaSpec);
    }

    
    // Lambda call, return type is array
    if(method.getName().equals("map")) {
      if(!(typeDef instanceof ObjectDef)) {
        // TODO
        throw new IllegalArgumentException("Not implemented!");
      }
      
      ObjectDef objectType = (ObjectDef) typeDef;
      if(objectType.getValues().size() != 1) {
        // TODO
        throw new IllegalArgumentException("Not implemented!");
      }
      
      TypeDef values = objectType.getValues().get(0);
      
      
      final String name = "staticblock.values";
/*
      if(body.getHitPolicy() instanceof HitPolicyMatrix) {
        HitPolicyMatrix hitPolicy = (HitPolicyMatrix) body.getHitPolicy();
        return ImmutableScalarDef.builder()
            .array(true)
            .required(false)
            .direction(DirectionType.IN)
            .type(hitPolicy.getToType())
            .name(name)
            .token(method.getToken())
            .build();
      }
      
      return ImmutableObjectDef.builder()
          .array(true)
          .required(false)
          .direction(DirectionType.IN)
          .values(body.getHeaders().getValues().stream()
              .filter(h -> h.getDirection() == DirectionType.OUT)
              .collect(Collectors.toList()))
          .name(name)
          .token(method.getToken())
          .build();
          */
    }
  
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getName()));
  }
  
  private final List<TypeDef> getStaticValues(AstNodeVisitorContext ctx) {

    // Just an assertion, security check that the method is actually there
    try {
      DecisionTableStaticValue.class.getMethod("getValues");
    } catch(Exception e) {
      throw new RuntimeException(e.getMessage(), e);
    }      
    
    
    DecisionTableBody body = getBody(ctx);
    Token token = ctx.getValue().getToken();
    
    final List<TypeDef> staticValues;
    if(body.getHitPolicy() instanceof HitPolicyFirst || 
       body.getHitPolicy() instanceof HitPolicyAll) {

      staticValues = Arrays.asList(ImmutableObjectDef.builder()
        .name(ExpressionVisitor.ACCESS_STATIC_VALUES).token(token)
        .array(true).required(true).direction(DirectionType.IN)
        .values(body.getHeaders().getValues().stream()
            .filter(h -> h.getDirection() == DirectionType.OUT)
            .map(h -> (ScalarDef) h)
            .filter(h -> h.getFormula().isEmpty())
            .collect(Collectors.toList()))
        .build());
      
    } else  {
      HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
      staticValues = new ArrayList<>(); 

      // matrix type
      staticValues.add(ImmutableObjectDef.builder()
        .name(ExpressionVisitor.ACCESS_STATIC_VALUES).token(ctx.getValue().getToken())
        .array(true).required(true).direction(DirectionType.IN)
        .addValues(ImmutableScalarDef.builder()
            .name("").token(token)
            .array(true).required(true).direction(DirectionType.IN)
            .type(matrix.getToType())
            .build())
        .build());
      
      // matrix row name and value
      for(MatrixRow row : matrix.getRows()) {
        staticValues.add(ImmutableScalarDef.builder()
            .name(row.getTypeName().getValue()).token(token)
            .array(true).required(true).direction(DirectionType.IN)
            .type(matrix.getToType())
            .build());
      }
    }
    return staticValues;
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
