package io.resys.hdes.compiler.spi.java.dt;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.FormulaMeta;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.ImmutableExecution;
import io.resys.hdes.executor.api.ImmutableFormulaMeta;

public class DtFrImplSpec {
  
  public static String ACCESS_INPUT_VALUE = "inputValue";
  public static String ACCESS_OUTPUT_VALUE = "outputValue";
  public static String ACCESS_STATIC_VALUE = "staticValue";

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
    
    public TypeSpec build(ScalarDef formula) {
      Assertions.notNull(body, () -> "body must be defined!");
      Assertions.notNull(formula, () -> "formula must be defined!");
      Assertions.isTrue(formula.getFormula().isPresent(), () -> "formula must be present!");
      
      ClassName outputType = namings.fr().outputValue(body, formula);
      EnScalarCodeSpec formulaSpec = ExpressionSpec.builder().parent(body).build(formula.getFormula().get());
      if(formula.getArray() != formulaSpec.getArray().orElse(false)) {
        throw new HdesCompilerException(HdesCompilerException.builder().dtFormulaContainsIncorectArrayType(formula, formulaSpec.getArray().orElse(false)));
      }
      
      if(formula.getType() != formulaSpec.getType()) {
        throw new HdesCompilerException(HdesCompilerException.builder().dtFormulaContainsIncorectScalarTypes(formula, formulaSpec.getType()));
      }
      
      CodeBlock.Builder execution = CodeBlock.builder()
      .addStatement("long start = System.currentTimeMillis()")
      .addStatement("var result = $L", formulaSpec.getValue())
      .addStatement("long end = System.currentTimeMillis()")
      .add("\r\n")
      .add("$T meta = $T.builder()", FormulaMeta.class, ImmutableFormulaMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
      .add("\r\n  ").add(".start(start).end(end).time(end - start)")
      .add("\r\n  ").addStatement(".build()")
      .add("\r\n")
      .addStatement("$T.Builder<$T, $T> builder = $T.builder()", ImmutableExecution.class, FormulaMeta.class, outputType, ImmutableExecution.class)
      .addStatement("return builder.meta(meta).value($L).build()", CodeBlock.builder()
          .add("$T.builder().$L(result).build()", JavaSpecUtil.immutable(outputType), formula.getName())
          .build()    
      );
      
      return TypeSpec.classBuilder(namings.fr().impl(body, formula))
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtFrImplSpec.class.getCanonicalName()).build())
          .addSuperinterface(namings.fr().api(body, formula))
          .addMethod(MethodSpec.methodBuilder("getSourceType")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(SourceType.class)
              .addStatement("return $T.FR", SourceType.class)
              .build())
          
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(namings.fr().inputValue(body, formula), ExpressionVisitor.ACCESS_SRC_VALUE).build())
              .returns(namings.fr().execution(body, formula))
              .addCode(execution.build())
              .build())
          .build();
    }
  }
}
