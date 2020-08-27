package io.resys.hdes.compiler.spi;

import java.util.stream.Collectors;

import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.ImmutableResource;
import io.resys.hdes.compiler.api.ImmutableTypeDeclaration;
import io.resys.hdes.compiler.api.ImmutableTypeName;
import io.resys.hdes.compiler.spi.dt.DtApiSpec;
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
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(JavaSpecUtil.javaFile(api, pkg)).build())
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(JavaSpecUtil.javaFile(impl, pkg)).build())
        
        .build();
  }
}
