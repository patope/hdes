package io.resys.hdes.compiler.spi.java.visitors.dt;

import java.util.stream.Collectors;

import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.AstEnvir;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompiler.Resource;
import io.resys.hdes.compiler.api.ImmutableResource;
import io.resys.hdes.compiler.api.ImmutableTypeDeclaration;
import io.resys.hdes.compiler.api.ImmutableTypeName;
import io.resys.hdes.compiler.spi.NamingContext;
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.executor.api.HdesExecutable;

public class DtDeclarationFactory {
  private DecisionTableBody body;
  private AstEnvir envir;
  private NamingContext naming;

  public static DtDeclarationFactory create() {
    return new DtDeclarationFactory();
  }

  public DtDeclarationFactory body(DecisionTableBody body) {
    this.body = body;
    return this;
  }

  public DtDeclarationFactory envir(AstEnvir envir) {
    this.envir = envir;
    return this;
  }

  public DtDeclarationFactory naming(NamingContext naming) {
    this.naming = naming;
    return this;
  }

  public Resource build() {
    Assertions.notNull(naming, () -> "naming context can't be null");
    Assertions.notNull(envir, () -> "envir can't be null");
    Assertions.notNull(body, () -> "body can't be null");

    final TypeSpec api = new DtImplementationVisitor(naming).visitDecisionTableBody(body);
    final TypeSpec impl = new DtInterfaceVisitor(naming).visitDecisionTableBody(body);
    
    final var pkg = naming.dt().pkg(body);
    final var nestedPkg = pkg + "." + api.name;
    
    return ImmutableResource.builder()
        .type(HdesExecutable.SourceType.DT)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        .addAllTypes(api.typeSpecs.stream().map(spec -> ImmutableTypeName.builder().name(spec.name).pkg(nestedPkg).build()).collect(Collectors.toList()))
        .addTypes(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
        .addTypes(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
        
        .input(JavaSpecUtil.typeName(naming.dt().inputValue(body)))
        .output(JavaSpecUtil.typeName(naming.dt().outputValueMono(body)))
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(api.name).pkg(pkg).build())
            .isExecutable(false).value(JavaSpecUtil.javaFile(api, pkg)).build())
        
        .addDeclarations(ImmutableTypeDeclaration.builder()
            .type(ImmutableTypeName.builder().name(impl.name).pkg(pkg).build())
            .isExecutable(true).value(JavaSpecUtil.javaFile(impl, pkg)).build())
        
        .build();
  }
}
