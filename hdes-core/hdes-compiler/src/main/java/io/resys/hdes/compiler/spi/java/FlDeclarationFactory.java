package io.resys.hdes.compiler.spi.java;

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
import java.util.stream.Collectors;

import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.ImmutableResource;
import io.resys.hdes.compiler.api.ImmutableTypeDeclaration;
import io.resys.hdes.compiler.api.ImmutableTypeName;
import io.resys.hdes.compiler.spi.java.fl.FlApiSpec;
import io.resys.hdes.compiler.spi.java.fl.FlSwitchApiSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable;

public class FlDeclarationFactory {
  private FlowBody body;
  private AstEnvir envir;
  private Namings naming;

  public static FlDeclarationFactory create() {
    return new FlDeclarationFactory();
  }

  public FlDeclarationFactory body(FlowBody body) {
    this.body = body;
    return this;
  }

  public FlDeclarationFactory envir(AstEnvir envir) {
    this.envir = envir;
    return this;
  }

  public FlDeclarationFactory naming(Namings naming) {
    this.naming = naming;
    return this;
  }

  public Resource build() {
    Assertions.notNull(naming, () -> "naming context can't be null");
    Assertions.notNull(envir, () -> "envir can't be null");
    Assertions.notNull(body, () -> "body can't be null");

    String pkg = naming.fl().pkg(body); 
    
    TypeSpec api = FlApiSpec.builder(naming).body(body).build();    
    TypeSpec impl = null; // new FlImplementationVisitor(naming).visitBody(body);
    List<TypeSpec> switchApi = FlSwitchApiSpec.builder(naming).body(body).build();
    
    return ImmutableResource.builder()
        .type(HdesExecutable.SourceType.FL)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        
        .input(JavaSpecUtil.typeName(naming.fl().inputValue(body)))
        .output(JavaSpecUtil.typeName(naming.fl().outputValue(body)))

        .addAllDeclarations(switchApi.stream()
            .map((TypeSpec e) -> ImmutableTypeDeclaration.builder()
                .type(ImmutableTypeName.builder().name(e.name).pkg(pkg).build())
                .isExecutable(false).value(JavaSpecUtil.javaFile(e, pkg)).build())
            .collect(Collectors.toList()))
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(JavaSpecUtil.javaFile(api, pkg)).build())
        
//        
//        .addDeclarations(ImmutableTypeDeclaration.builder()
//            .type(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
//            .isExecutable(true).value(JavaSpecUtil.javaFile(impl, pkg)).build())

        .build();
  }
}
