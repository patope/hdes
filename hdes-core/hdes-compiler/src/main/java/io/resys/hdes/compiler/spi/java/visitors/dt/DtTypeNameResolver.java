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
    
    // static reference
    if(pathName[0].equals("static")) {
      
    }
    
    
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
