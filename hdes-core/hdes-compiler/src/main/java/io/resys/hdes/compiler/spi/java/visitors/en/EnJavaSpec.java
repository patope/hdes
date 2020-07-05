package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;

public interface EnJavaSpec {

  @FunctionalInterface
  interface TypeNameResolver {
    TypeDefNode accept(TypeName name);
  }
  
  @Value.Immutable
  interface EnRefSpec extends EnJavaSpec {
    List<AstNode> getValues();
  }
}
