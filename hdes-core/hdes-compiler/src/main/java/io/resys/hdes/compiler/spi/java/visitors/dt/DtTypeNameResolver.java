package io.resys.hdes.compiler.spi.java.visitors.dt;

import java.util.Arrays;
import java.util.Optional;

import io.resys.hdes.ast.api.nodes.AstNode.ArrayTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.TypeNameResolver;

public class DtTypeNameResolver implements TypeNameResolver {
  private final DecisionTableBody body;

  public DtTypeNameResolver(DecisionTableBody body) {
    super();
    this.body = body;
  }

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
    
    } else if(node instanceof ArrayTypeDefNode) {
      // array access not supported
    }
    return Optional.empty();
    
  }
}