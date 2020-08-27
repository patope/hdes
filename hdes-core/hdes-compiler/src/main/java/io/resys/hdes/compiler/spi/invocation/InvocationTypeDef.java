package io.resys.hdes.compiler.spi.invocation;

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
        return new InvocationTypeDefDt(envir);
      }
      return new InvocationTypeDefFl(envir);  
    }
  }
}
