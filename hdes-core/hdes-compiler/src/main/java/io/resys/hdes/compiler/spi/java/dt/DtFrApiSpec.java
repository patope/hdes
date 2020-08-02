package io.resys.hdes.compiler.spi.java.dt;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.InvocationSpecParams;
import io.resys.hdes.compiler.spi.java.en.ExpressionInvocationSpec.UsageSource;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.HdesExecutable;

public class DtFrApiSpec {

  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private DecisionTableBody body;
    
    private Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }
    
    private TypeSpec returnType(ScalarDef scalar) {
      MethodSpec method = MethodSpec.methodBuilder(JavaSpecUtil.methodName(scalar.getName()))
        .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
        .returns(JavaSpecUtil.typeName(scalar.getType()))
        .build();
      ClassName outputType = namings.fr().outputValue(body, scalar);
      return JavaSpecUtil.immutableSpec(outputType)
        .addSuperinterface(HdesExecutable.OutputValue.class)
        .addMethod(method)
        .build();
    }
    
    private TypeSpec inputType(ScalarDef scalar) {
      InvocationSpecParams referedTypes = ExpressionInvocationSpec.builder().parent(body).build(scalar.getFormula().get());
      if(scalar.getDirection() == DirectionType.IN && referedTypes.getUsageSources().contains(UsageSource.OUT)) {
        List<String> unusables = referedTypes.getValues().stream()
            .filter(e -> e.getUsageSource() == UsageSource.OUT)
            .map(e -> e.getNode().getName()).collect(Collectors.toList());
        throw new HdesCompilerException(HdesCompilerException.builder().dtFormulaContainsIncorectScopeParameters(scalar, unusables));
      }
      
      List<MethodSpec> methods = new ArrayList<>();
      for(UsageSource scope : referedTypes.getUsageSources()) {
        switch (scope) {
        case IN:
          methods.add(MethodSpec.methodBuilder(JavaSpecUtil.methodName(ExpressionVisitor.ACCESS_INPUT_VALUE))
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(namings.dt().inputValue(body))
              .build());
          break;
        case OUT:
          methods.add(MethodSpec.methodBuilder(JavaSpecUtil.methodName(ExpressionVisitor.ACCESS_OUTPUT_VALUE))
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(namings.dt().outputValueMono(body))
              .build());
          break;
        case STATIC:
          methods.add(MethodSpec.methodBuilder(JavaSpecUtil.methodName(ExpressionVisitor.ACCESS_STATIC_VALUE))
              .addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT)
              .returns(namings.dt().staticValue(body))
              .build());
        case INSTANCE:
          continue;
        default: throw new IllegalArgumentException("Scope: " + scope + " parameter: " + scalar + " not implemented!"); 
        }
      }
      return JavaSpecUtil
          .immutableSpec(namings.fr().inputValue(body, scalar))
          .addSuperinterface(HdesExecutable.InputValue.class)
          .addMethods(methods)
          .build();
    }
    
    public TypeSpec build(ScalarDef formula) {
      Assertions.notNull(body, () -> "body must be defined!");
      Assertions.notNull(formula, () -> "formula must be defined!");
      Assertions.isTrue(formula.getFormula().isPresent(), () -> "formula must be present!");
      
      return TypeSpec.interfaceBuilder(namings.fr().api(body, formula))
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtFrApiSpec.class.getCanonicalName()).build())
          .addSuperinterface(namings.fr().executable(body, formula))
          .addType(inputType(formula))
          .addType(returnType(formula))
          .build();
    }
  }
}
