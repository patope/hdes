package io.resys.hdes.compiler.spi.java.en;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.spi.Assertions;

public class ExpressionRefsSpec {

  @Value.Immutable
  public interface EnReferedTypes {
    List<EnReferedType> getValues();
    Set<EnReferedScope> getScopes();
  }
  
  @Value.Immutable
  public interface EnReferedType {
    TypeDef getNode();
    Optional<TypeInvocation> getTypeName();
    Optional<MethodInvocation> getMethodRef(); 
    EnReferedScope getScope();
    List<EnReferedType> getChildren();
  }
  
  public interface EnReferedTypeResolver {
    TypeDef accept(TypeInvocation name);
    TypeDef accept(MethodInvocation name);
  }
  
  public static enum EnReferedScope {
    INSTANCE, STATIC, LAMBDA, IN, OUT, METHOD
  }
  
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
    
    public EnReferedTypes build() {
      return new ExpressionRefsVisitor(resolver).visitBody(body);
    }
  }
}
