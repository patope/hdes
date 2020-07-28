package io.resys.hdes.compiler.spi.java.dt;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.ImmutableScalarTypeDefNode;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.en.EnReferedTypesSpec;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtEnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtFormulaVisitor;
import io.resys.hdes.compiler.spi.naming.Namings;

public class DtFrApiSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private DecisionTableBody body;
    private DtEnReferedTypeResolver resolver;
    
    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(DecisionTableBody body) {
      this.body = body;
      resolver = new DtEnReferedTypeResolver(body);
      return this;
    }
    
    public TypeSpec build(ScalarTypeDefNode formula) {
      Assertions.notNull(body, () -> "body must be defined!");
      Assertions.notNull(formula, () -> "formula must be defined!");
      Assertions.isTrue(formula.getFormula().isPresent(), () -> "formula must be present!");
      
      ScalarTypeDefNode outputReturnType = ImmutableScalarTypeDefNode.builder().from(formula).required(Boolean.TRUE).build();
      
      //List<TypeDefNode> expressionTypes = new EnInterfaceVisitor(this.resolver).visitExpressionBody(typeDef.getFormula().get());
      ClassName outputType = namings.fr().outputValue(body, formula);
      
      
      EnReferedTypesSpec.builder(resolver).body(formula.getFormula().get()).build();
      
      
      return TypeSpec.interfaceBuilder(namings.fr().api(body, formula))
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class)
              .addMember("value", "$S", DtFormulaVisitor.class.getCanonicalName())
              .build())
          .addSuperinterface(namings.fr().executable(body, formula))
          
          /*
          // input
          .addTypes(visitInputs(expressionTypes, formula).getValues())
          
          // output
          .addType(JavaSpecUtil.immutableSpec(outputType)
              .addSuperinterface(HdesExecutable.OutputValue.class)
              .addMethod(visitTypeDef(outputReturnType, outputReturnType).getValue())
              .build())
          */
          
          .build();
    }
  }
}
