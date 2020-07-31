package io.resys.hdes.compiler.spi.java.visitors.fl;

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

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.InvocationResolver;

public class FlTypeNameResolver implements InvocationResolver {
  private final FlowBody body;
  private final AstEnvir astEnvir;

  public FlTypeNameResolver(FlowBody body, AstEnvir astEnvir) {
    super();
    this.body = body;
    this.astEnvir = astEnvir;
  }

  @Override
  public TypeDef accept(Invocation invocation) {
    if(invocation instanceof TypeInvocation) {
      return accept((TypeInvocation) invocation);
    }
    return accept((MethodInvocation) invocation);
  }
  
  
  public TypeDef accept(TypeInvocation typeName) {
    String[] pathName = typeName.getValue().split("\\.");
    
    // Find from inputs
    Optional<TypeDef> typeDef = body.getHeaders().getValues().stream()
        .filter(f -> f.getDirection() == DirectionType.IN)
        .map(f -> getTypeDefNode(f, pathName))
        .filter(f -> f.isPresent())
        .map(f -> f.get()).findFirst();
    
    // find from tasks
    if(typeDef.isEmpty() && body.getTask().isPresent()) {
      typeDef = getTypeDefNode(body.getTask().get(), pathName);
    }
    
    if(typeDef.isPresent()) {
      return typeDef.get();
    }
    
    throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(typeName));
  }

  private Optional<TypeDef> getTypeDefNode(FlowTaskNode node, String[] pathName) {
    String taskName = pathName[0];
    if(node.getId().equals(taskName)) {
      
      // No outputs connected
      if(node.getRef().isEmpty()) {
        return Optional.empty();
      }
      Body bodyNode = astEnvir.getByAstId(node.getRef().get().getValue());
      String[] nextPath = Arrays.copyOfRange(pathName, 1, pathName.length - 1);
      Optional<TypeDef> typeDef = bodyNode.getHeaders().getValues().stream()
          .filter(f -> f.getDirection() == DirectionType.OUT)
          .map(f -> getTypeDefNode(f, nextPath))
          .filter(f -> f.isPresent())
          .map(f -> f.get()).findFirst();
      return typeDef;
    }
    
    FlowTaskPointer pointer = node.getNext();
    if (pointer instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) node;
      FlowTaskNode next = then.getTask().get();
      return getTypeDefNode(next, pathName);
      
    } else if (pointer instanceof WhenThenPointer) {
      WhenThenPointer whenThen = (WhenThenPointer) node;
      for (WhenThen c : whenThen.getValues()) {
        FlowTaskPointer next = c.getThen();
        if(next instanceof ThenPointer) {
          return getTypeDefNode(((ThenPointer)next).getTask().get(), pathName);
        }
      }
    }
    
    return Optional.empty();
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


  public TypeDef accept(MethodInvocation name) {
    // TODO Auto-generated method stub
    return null;
  }
}
