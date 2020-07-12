package io.resys.hdes.compiler.spi.java;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.ImmutableAstEnvir;
import io.resys.hdes.compiler.api.HdesCompiler;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtDeclarationFactory;
import io.resys.hdes.compiler.spi.java.visitors.fl.FlDeclarationFactory;
import io.resys.hdes.compiler.spi.naming.JavaNaming;
import io.resys.hdes.compiler.spi.naming.Namings;

public class JavaHdesCompiler implements HdesCompiler {
  @Override
  public Parser parser() {
    AstEnvir.Builder builder = ImmutableAstEnvir.builder();
    return new Parser() {
      @Override
      public List<Resource> build() {
        final AstEnvir envir = builder.build();
        final Namings naming = JavaNaming.config().ast(envir).build();
        final List<Resource> values = envir.getBody().values().stream()
          .map(ast -> {
            if (ast instanceof DecisionTableBody) {
              return DtDeclarationFactory.create().body((DecisionTableBody) ast).envir(envir).naming(naming).build();
            } else if (ast instanceof FlowBody) {
              return FlDeclarationFactory.create().body((FlowBody) ast).envir(envir).naming(naming).build();
            } else {
              throw new HdesCompilerException(HdesCompilerException.builder().unknownAst(ast));
            }
          })
          .collect(Collectors.toList());
        return Collections.unmodifiableList(values);
      }

      @Override
      public Parser add(String filename, String src) {
        builder.add().externalId(filename).src(src);
        return this;
      }
    };
  }

  public static Config config() {
    return new Config();
  }

  public static class Config {
    public JavaHdesCompiler build() {
      return new JavaHdesCompiler();
    }
  }
}
