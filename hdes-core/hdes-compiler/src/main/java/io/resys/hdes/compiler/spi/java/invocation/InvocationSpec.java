package io.resys.hdes.compiler.spi.java.invocation;

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

import java.util.List;
import java.util.Set;

import org.immutables.value.Value;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Body;
import io.resys.hdes.ast.api.nodes.AstNode.Invocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.AstNodeVisitorContext;
import io.resys.hdes.ast.api.nodes.ImmutableAstNodeVisitorContext;
import io.resys.hdes.ast.spi.Assertions;

public interface InvocationSpec {

  @Value.Immutable
  interface InvocationSpecParams {
    List<InvocationSpecParam> getValues();
    Set<InvocationType> getTypes();
    TypeDef getReturnType();
  }

  @Value.Immutable
  interface InvocationSpecParam {
    TypeDef getNode();
    Invocation getValue();
    InvocationType getType();
  }

  static enum InvocationType {
    INSTANCE, STATIC, IN, OUT
  }

  public static Builder builder() {
    return new Builder();
  }
  
  public static class Builder {
    private AstNodeVisitorContext ctx;
    private Body node;
    private AstEnvir envir;

    public Builder parent(Body node) {
      this.node = node;
      return this;
    }
    public Builder envir(AstEnvir envir) {
      this.envir = envir;
      return this;
    }
    public Builder ctx(AstNodeVisitorContext ctx) {
      this.ctx = ctx;
      return this;
    }

    public InvocationSpecParams build(AstNode value) {
      Assertions.isTrue(node != null || ctx != null, () -> "node or context can't be null!");
      Assertions.notNull(envir, () -> "envir or context can't be null!");

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
      InvocationTypeDef resolver = InvocationTypeDef.builder().envir(envir).body(body).build();
      return new InvocationSpecVisitor(resolver).visit(value, ctx);
    }
  }
}
