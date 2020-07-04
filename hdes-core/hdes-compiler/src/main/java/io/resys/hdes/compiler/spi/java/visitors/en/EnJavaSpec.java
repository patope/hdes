package io.resys.hdes.compiler.spi.java.visitors.en;

import java.util.List;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.AstNode.TypeName;

public interface EnJavaSpec {

  @Value.Immutable
  interface EnRefSpec extends EnJavaSpec {
    List<TypeName> getValues();
  }
}
