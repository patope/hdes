package io.resys.hdes.compiler.spi.java.dt;

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
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ImmutableObjectDef;
import io.resys.hdes.ast.api.nodes.ImmutableScalarDef;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableStaticValue;

public class DtParameterResolver implements InvocationResolver {
  private final DecisionTableBody body;

  public DtParameterResolver(DecisionTableBody body) {
    super();
    this.body = body;
  }
  
  @Override
  public TypeDef accept(Invocation invocation) {
    if(invocation instanceof TypeInvocation) {
      return accept((TypeInvocation) invocation);
    }
    return accept((MethodInvocation) invocation);
  }
  
  /*
   *     // static variable reference
    if(typeName.getScope() == TypeNameScope.STATIC && pathName.length == 1) {
      
      TypeDefNode value = null;
      if(body.getHitPolicy() instanceof HitPolicyMatrix) {
        HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
        ScalarType scalarType = matrix.getToType();
        value = ImmutableScalarDef.builder()
            .direction(DirectionType.IN)
            .token(typeName.getToken())
            .required(true)
            .type(scalarType)
            .name("values")
            .build();
      }
      
      return ImmutableArrayTypeDefNode.builder()
      .token(typeName.getToken())
      .direction(DirectionType.IN)
      .required(true)
      .value(value)
      .name(pathName[0] + "Block")
      .build();
    }
   */


  private TypeDef accept(TypeInvocation typeName) {
    String[] pathName = typeName.getValue().split("\\.");
        
    // static data access
    if(typeName.getScope() == TypeNameScope.STATIC) {
      
      final List<TypeDef> staticValues;
      if(body.getHitPolicy() instanceof HitPolicyFirst || 
         body.getHitPolicy() instanceof HitPolicyAll) {

        staticValues = Arrays.asList(ImmutableObjectDef.builder()
          .name(ExpressionVisitor.ACCESS_STATIC_VALUES).token(typeName.getToken())
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
          .name(ExpressionVisitor.ACCESS_STATIC_VALUES).token(typeName.getToken())
          .array(true).required(true).direction(DirectionType.IN)
          .addValues(ImmutableScalarDef.builder()
              .name("").token(typeName.getToken())
              .array(true).required(true).direction(DirectionType.IN)
              .type(matrix.getToType())
              .build())
          .build());
        
        // matrix row name and value
        for(MatrixRow row : matrix.getRows()) {
          staticValues.add(ImmutableScalarDef.builder()
              .name(row.getTypeName().getValue()).token(typeName.getToken())
              .array(true).required(true).direction(DirectionType.IN)
              .type(matrix.getToType())
              .build());
        }
      }

      
      // Just an assertion, security check that the method is actually there
      try {
        DecisionTableStaticValue.class.getMethod("getValues");
      } catch(Exception e) {
        throw new RuntimeException(e.getMessage(), e);
      }      
      
      return ImmutableObjectDef.builder()
          .array(false)
          .required(true)
          .direction(DirectionType.IN)
          .addAllValues(staticValues)
          .name(typeName.getValue())
          .token(typeName.getToken())
          .build();
    }
    
    // Find from inputs
    Optional<TypeDef> typeDef = body.getHeaders().getValues().stream()
        .map(f -> getTypeDefNode(f, pathName))
        .filter(f -> f.isPresent())
        .map(f -> f.get()).findFirst();
    
    if(typeDef.isPresent()) {
      return typeDef.get();
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
  }
  
  private Optional<TypeDef> getTypeDefNode(TypeDef node, String[] pathName) {
    String path = pathName[0];
    if (!node.getName().equals(path)) {
      return Optional.empty();
    }
    
    if (pathName.length == 1) {
      return Optional.of(node);
    }
    String[] nextPath = Arrays.copyOfRange(pathName, 1, pathName.length - 1);
    
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

  private TypeDef accept(MethodInvocation method) {
    
    final boolean isGlobal = method.getType().isEmpty();
    
    if(isGlobal) {
      Set<ScalarType> typesUsed = new HashSet<>();
      for(AstNode value : method.getValues()) {
  
        
        if(value instanceof ExpressionBody) {
          //ExpressionRefsSpec.builder(this).body(value);        
          System.out.println(value);  
        } else if(value instanceof MethodInvocation) {
          
          TypeDef child = accept((MethodInvocation) value);
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
    TypeDef typeDef = accept(typeName);
    
    // type + lambda expression
    if( method.getValues().size() == 1 && 
        method.getName().equals("map") &&
        method.getValues().get(0) instanceof LambdaExpression) {
      
      LambdaExpression lambda = (LambdaExpression) method.getValues().get(0);
      
      System.out.println(lambda);
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
    }
  
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getName()));
  }
}
