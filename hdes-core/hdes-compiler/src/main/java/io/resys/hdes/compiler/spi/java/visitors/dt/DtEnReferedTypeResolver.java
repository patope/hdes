package io.resys.hdes.compiler.spi.java.visitors.dt;

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
import java.util.Optional;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodRefNode;
import io.resys.hdes.ast.api.nodes.ImmutableObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.ImmutableScalarTypeDefNode;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec.EnReferedTypeResolver;

public class DtEnReferedTypeResolver implements EnReferedTypeResolver {
  private final DecisionTableBody body;

  public DtEnReferedTypeResolver(DecisionTableBody body) {
    super();
    this.body = body;
  }
  
  /*
   *     // static variable reference
    if(typeName.getScope() == TypeNameScope.STATIC && pathName.length == 1) {
      
      TypeDefNode value = null;
      if(body.getHitPolicy() instanceof HitPolicyMatrix) {
        HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
        ScalarType scalarType = matrix.getToType();
        value = ImmutableScalarTypeDefNode.builder()
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

  @Override
  public TypeDefNode accept(TypeName typeName) {
    String[] pathName = typeName.getValue().split("\\.");
        
    // Find from inputs
    Optional<TypeDefNode> typeDef = body.getHeaders().getValues().stream()
        .map(f -> getTypeDefNode(f, pathName))
        .filter(f -> f.isPresent())
        .map(f -> f.get()).findFirst();
    
    if(typeDef.isPresent()) {
      return typeDef.get();
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
  }
  
  private Optional<TypeDefNode> getTypeDefNode(TypeDefNode node, String[] pathName) {
    String path = pathName[0];
    if (!node.getName().equals(path)) {
      return Optional.empty();
    }
    
    if (pathName.length == 1) {
      return Optional.of(node);
    }
    String[] nextPath = Arrays.copyOfRange(pathName, 1, pathName.length - 1);
    
    // Nested structure
    if(node instanceof ObjectTypeDefNode) {
      ObjectTypeDefNode objectDefNode = (ObjectTypeDefNode) node;
      
      for(TypeDefNode nextNode : objectDefNode.getValues()) {
        Optional<TypeDefNode> nextResult = getTypeDefNode(nextNode, nextPath);
        if(nextResult.isPresent()) {
          return nextResult;
        }
      }
      
      return Optional.empty();
    
    }
    return Optional.empty();
  }

  @Override
  public TypeDefNode accept(MethodRefNode method) {
    Assertions.isTrue(method.getType().isPresent(), () -> "DT method ref must contain type definition");
    TypeName typeName = method.getType().get();
    String[] pathName = typeName.getValue().split("\\.");
    
    if(typeName.getScope() == TypeNameScope.STATIC && pathName.length == 1) {
      // values access
      if(method.getName().equals("map")) {
        
        final String name = "staticblock.values";

        if(body.getHitPolicy() instanceof HitPolicyMatrix) {
          HitPolicyMatrix hitPolicy = (HitPolicyMatrix) body.getHitPolicy();
          return ImmutableScalarTypeDefNode.builder()
              .array(true)
              .required(false)
              .direction(DirectionType.OUT)
              .type(hitPolicy.getToType())
              .name(name)
              .token(method.getToken())
              .build();
        }
        
        return ImmutableObjectTypeDefNode.builder()
            .array(true)
            .required(false)
            .values(body.getHeaders().getValues().stream()
                .filter(h -> h.getDirection() == DirectionType.OUT)
                .collect(Collectors.toList()))
            .name(name)
            .token(method.getToken())
            .build();
      }
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownFunctionCall(method, method.getName()));
  }
}
