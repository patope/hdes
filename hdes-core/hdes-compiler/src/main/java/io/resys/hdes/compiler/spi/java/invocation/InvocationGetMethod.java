package io.resys.hdes.compiler.spi.java.invocation;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;

public class InvocationGetMethod {
  public final static String ACCESS_INPUT_VALUE = "inputValue";
  
  static enum InvocationType {
    INSTANCE, STATIC, IN, OUT
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private AstNodeVisitorContext ctx;
    private Invocation invocation;
    private TypeDef typeDef;

    public Builder invocation(Invocation invocation) {
      this.invocation = invocation;
      return this;
    }
    public Builder ctx(AstNodeVisitorContext ctx) {
      this.ctx = ctx;
      return this;
    }
    public Builder typeDef(TypeDef typeDef) {
      this.typeDef = typeDef;
      return this;
    }

    public CodeBlock build() {
      Assertions.notNull(ctx, () -> "context can't be null!");
      Assertions.notNull(invocation, () -> "invocation can't be null!");
      Assertions.notNull(typeDef, () -> "typeDef can't be null!");

      Body body = TypeDefFinder.getBody(ctx);
      if(body instanceof DecisionTableBody) {
        return new InvocationGetMethodDt().apply(ctx, invocation, typeDef);
      }
      
      return new InvocationGetMethodFl().apply(ctx, invocation, typeDef);
    }
  }
}
