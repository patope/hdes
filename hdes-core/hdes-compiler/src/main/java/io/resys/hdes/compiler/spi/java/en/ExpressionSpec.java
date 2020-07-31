package io.resys.hdes.compiler.spi.java.en;

import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;

public class ExpressionSpec {
  
  public static Builder builder(InvocationResolver namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final InvocationResolver resolver;
    private ExpressionBody body;

    private Builder(InvocationResolver resolver) {
      super();
      this.resolver = resolver;
    }

    public Builder body(ExpressionBody body) {
      this.body = body;
      return this;
    }
    
    public EnScalarCodeSpec build() {
      AstNodeVisitorContext ctx = ImmutableAstNodeVisitorContext.builder().value(body).build();
      return new ExpressionVisitor(resolver).visitBody(body, ctx);
    }
  }
}
