package io.resys.hdes.compiler.spi.java.en;

import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;

public class ExpressionSpec {
  
  public static Builder builder() {
    return new Builder();
  }

  public static class Builder {
    private AstNodeVisitorContext ctx;
    private Body node;

    public Builder parent(Body node) {
      this.node = node;
      return this;
    }
    
    public Builder ctx(AstNodeVisitorContext ctx) {
      this.ctx = ctx;
      return this;
    }
    
    public EnScalarCodeSpec build(ExpressionBody value) {
      Assertions.isTrue(node != null || ctx != null, () -> "node or context can't be null!");

      // find body node
      Body body = null;
      if(ctx != null) {
        AstNodeVisitorContext parent = ctx;
        do {
          if(parent.getValue() instanceof Body) {
            body = (Body) parent.getValue();
          } else {
            parent = parent.getParent().orElse(null);
          }
        } while(body == null && parent != null);
      } else if(node != null) {
        body = node;
        AstNodeVisitorContext parent = ImmutableAstNodeVisitorContext.builder().value(body).build();
        ctx = ImmutableAstNodeVisitorContext.builder().value(value).parent(parent).build();
      }
      
      InvocationResolver resolver = null;
      if (body instanceof DecisionTableBody) {
        resolver = new DtInvocationResolver();
      } 
      
      Assertions.notNull(resolver, () -> "can't create resolver for node: " + node + " ctx: " + ctx);
      
      return new ExpressionVisitor(resolver).visitBody(value, ctx);
    }
  }
}