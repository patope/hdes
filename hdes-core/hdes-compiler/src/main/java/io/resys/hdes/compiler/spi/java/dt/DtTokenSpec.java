package io.resys.hdes.compiler.spi.java.dt;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.executor.api.ImmutableMetaStamp;
import io.resys.hdes.executor.api.ImmutableMetaToken;

public class DtTokenSpec {

  public static CodeBlock build(AstNode node, String value) {
    return CodeBlock.builder().add("$T.builder()", ImmutableMetaToken.class)
        .add("\r\n    .value($S)", value.replaceAll("\\r|\\n", " ").replaceAll("\\s{2,}", " "))
        .add("\r\n    .start($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, node.getToken().getStartLine(), node.getToken().getStartCol())
        .add("\r\n    .end($T.builder().line($L).column($L).build())", ImmutableMetaStamp.class, node.getToken().getEndLine(), node.getToken().getEndCol())
        .add("\r\n    .build()")
        .build();
  }
}
