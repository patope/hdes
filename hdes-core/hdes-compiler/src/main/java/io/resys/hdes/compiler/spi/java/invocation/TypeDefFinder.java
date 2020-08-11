package io.resys.hdes.compiler.spi.java.invocation;

import java.util.Arrays;
import java.util.Optional;

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
