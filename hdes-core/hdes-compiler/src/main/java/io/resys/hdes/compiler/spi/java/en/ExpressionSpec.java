package io.resys.hdes.compiler.spi.java.en;

import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;

public class ExpressionSpec {
  
  public static Builder builder(EnReferedTypeResolver namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final EnReferedTypeResolver resolver;
    private ExpressionBody body;

    private Builder(EnReferedTypeResolver resolver) {
      super();
      this.resolver = resolver;
    }

    public Builder body(ExpressionBody body) {
      this.body = body;
      return this;
    }
    
    public EnScalarCodeSpec build() {
      return new ExpressionVisitor(resolver).visitExpressionBody(body);
    }
  }
}
