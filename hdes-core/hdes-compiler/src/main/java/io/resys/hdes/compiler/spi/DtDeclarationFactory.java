package io.resys.hdes.compiler.spi;

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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.ImmutableResource;
import io.resys.hdes.compiler.api.ImmutableTypeDeclaration;
import io.resys.hdes.compiler.api.ImmutableTypeName;
import io.resys.hdes.compiler.spi.dt.DtApiSpec;
import io.resys.hdes.compiler.spi.dt.DtFrApiSpec;
import io.resys.hdes.compiler.spi.dt.DtFrImplSpec;
import io.resys.hdes.compiler.spi.dt.DtImplSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.executor.api.HdesExecutable;

public class DtDeclarationFactory {
  private DecisionTableBody body;
  private CompilerContext ctx;

  public static DtDeclarationFactory create() {
    return new DtDeclarationFactory();
  }

  public DtDeclarationFactory body(DecisionTableBody body) {
    this.body = body;
    return this;
  }

  public DtDeclarationFactory ctx(CompilerContext ctx) {
    this.ctx = ctx;
    return this;
  }

  public Resource build() {
    Assertions.notNull(ctx, () -> "ctx context can't be null");
    Assertions.notNull(body, () -> "body can't be null");

    final TypeSpec api = DtApiSpec.builder(ctx).body(body).build();
    final TypeSpec impl = DtImplSpec.builder(ctx).body(body).build();
    
    //final List<TypeSpec> formulaApis = new DtFormulaVisitor(naming).visitDecisionTableBody(body);
    
    final List<TypeSpec> formulas = new ArrayList<>();
    body.getHeaders().getValues().stream()
        .map(h -> (ScalarDef) h)
        .filter(h -> h.getFormula().isPresent())
        .forEach(f -> {
          formulas.add(DtFrApiSpec.builder(ctx).body(body).build(f));
          formulas.add(DtFrImplSpec.builder(ctx).body(body).build(f));
        });
    
    final var pkg = ctx.pkg(body);
    final var nestedPkg = pkg + "." + api.name;
    
    return ImmutableResource.builder()
        .type(HdesExecutable.SourceType.DT)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        .addAllTypes(api.typeSpecs.stream().map(spec -> ImmutableTypeName.builder().name(spec.name).pkg(nestedPkg).build()).collect(Collectors.toList()))
        .addTypes(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
        .addTypes(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
        
        .input(JavaSpecUtil.typeName(ctx.dt().inputValue(body)))
        .output(JavaSpecUtil.typeName(ctx.dt().outputValueMono(body)))
        
        .addAllDeclarations(formulas.stream().map(s -> ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(s.name).pkg(pkg).build())
            .isExecutable(false).value(JavaSpecUtil.javaFile(s, pkg)).build())
            .collect(Collectors.toList()))
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(JavaSpecUtil.javaFile(api, pkg)).build())
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(JavaSpecUtil.javaFile(impl, pkg)).build())
        
        .build();
  }
}
