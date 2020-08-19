package io.resys.hdes.compiler.spi.java.fl;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.Namings;

public class FlApiSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private FlowBody body;

    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(FlowBody body) {
      this.body = body;
      return this;
    }
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final ClassName interfaceName = namings.fl().api(body);
      final TypeName superinterface = namings.fl().executable(body);
      final AnnotationSpec annotation = AnnotationSpec.builder(javax.annotation.processing.Generated.class)
          .addMember("value", "$S", FlApiSpec.class.getCanonicalName()).build();

      return TypeSpec.interfaceBuilder(interfaceName).addModifiers(Modifier.PUBLIC).addAnnotation(annotation)
        .addSuperinterface(superinterface)
        .addTypes(FlHeadersSpec.builder(namings).body(body).build())
        .addType(FlStateSpec.builder(namings).body(body).build())
        .build();
    }
  }
}
