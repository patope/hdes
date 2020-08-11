package io.resys.hdes.compiler.spi.java.invocation;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;

public interface InvocationTypeDef {
  
  TypeDef getTypeDef(Invocation name, AstNodeVisitorContext ctx);
  
  CodeBlock getMethod(Invocation name, AstNodeVisitorContext ctx);
  
  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private AstEnvir envir;
    private Body body;

    public Builder envir(AstEnvir envir) {
      this.envir = envir;
      return this;
    }
    public Builder body(Body body) {
      this.body = body;
      return this;
    }
    
    public InvocationTypeDef build() {
      Assertions.notNull(envir, () -> "envir can't be null!");
      Assertions.notNull(body, () -> "body can't be null!");
      
      if (body instanceof DecisionTableBody) {
        return new InvocationTypeDefGeneric(envir);
      }
      return new InvocationTypeDefFl(envir);  
    }
  }
}
