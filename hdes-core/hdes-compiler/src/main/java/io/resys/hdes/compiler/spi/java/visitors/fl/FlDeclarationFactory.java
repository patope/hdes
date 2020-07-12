package io.resys.hdes.compiler.spi.java.visitors.fl;

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
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
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

    TypeSpec api = new FlInterfaceVisitor(naming).visitBody(body);
    TypeSpec impl = new FlImplementationVisitor(naming).visitBody(body);
    List<TypeSpec> switches = new FlSwitchVisitor(naming).visitBody(body);
    

    String pkg = naming.fl().pkg(body); 
    
    return ImmutableResource.builder()
        .type(HdesExecutable.SourceType.FL)
        .name(body.getId().getValue())
        .source(body.getToken().getText())
        .ast(body)
        
        .input(JavaSpecUtil.typeName(naming.fl().inputValue(body)))
        .output(JavaSpecUtil.typeName(naming.fl().outputValue(body)))

        .addAllDeclarations(switches.stream().map(s -> ImmutableTypeDeclaration.builder()
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
