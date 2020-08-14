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

import java.util.Arrays;
import java.util.Optional;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.spi.Assertions;

public class TypeDefFinder {
  
  public static Optional<AstNodeVisitorContext> getLambda(AstNodeVisitorContext ctx) {
    AstNodeVisitorContext parent = ctx;
    do {
      if(parent.getValue() instanceof LambdaExpression) {
        return Optional.of(parent);
      } else {
        parent = parent.getParent().orElse(null);
      }
    } while(parent != null);
    
    return Optional.empty();
  }

  public static Optional<FlowTaskNode> getTask(Optional<FlowTaskNode> start, String name) {
    if (start.isEmpty()) {
      return Optional.empty();
    }
    
    if(start.get().getId().equals(name)) {
      return start;
    }

    FlowTaskPointer pointer = start.get().getNext();
    if (pointer instanceof ThenPointer) {
      ThenPointer then = (ThenPointer) pointer;
      return getTask(then.getTask(), name);
      
    } else if (pointer instanceof WhenThenPointer) {
      WhenThenPointer whenThen = (WhenThenPointer) pointer;
      
      for (WhenThen c : whenThen.getValues()) {
        FlowTaskPointer nextPointer = c.getThen();
        if(nextPointer instanceof ThenPointer) {
          ThenPointer next = (ThenPointer) nextPointer;
          Optional<FlowTaskNode> result = getTask(next.getTask(), name);
          if(result.isPresent()) {
            return result;
          }
        }
      }
    }
    
    return Optional.empty();
  }
  
  
  public static Optional<TypeDef> getTypeDef(TypeDef node, String[] pathName) {
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
        Optional<TypeDef> nextResult = getTypeDef(nextNode, nextPath);
        if(nextResult.isPresent()) {
          return nextResult;
        }
      }
      
      return Optional.empty();
    
    }
    return Optional.empty();
  }
  
  public static Optional<TypeDef> getTypeDef(Body body, String[] pathName) {
    return body.getHeaders().getValues().stream()
      .map(f -> getTypeDef(f, pathName))
      .filter(f -> f.isPresent())
      .map(f -> f.get())
      .findFirst();    
  }

  @SuppressWarnings("unchecked")
  public static <T extends AstNode> Optional<T> getNode(Class<T> type, AstNodeVisitorContext ctx) {
    AstNodeVisitorContext parent = ctx;
    do {
      if(type.isAssignableFrom(parent.getValue().getClass())) {
        return Optional.of((T) parent.getValue());
      } else {
        parent = parent.getParent().orElse(null);
      }
    } while(parent != null);
    
    return Optional.empty();
  }
  
  @SuppressWarnings("unchecked")
  public static <T extends Body> T getBody(AstNodeVisitorContext ctx) {
    AstNodeVisitorContext parent = ctx;
    do {
      if(parent.getValue() instanceof Body) {
        return (T) parent.getValue();
      } else {
        parent = parent.getParent().orElse(null);
      }
    } while(parent != null);
    
    return Assertions.fail(() -> "Can't find decision table body for ctx: " + ctx);
  }
}
