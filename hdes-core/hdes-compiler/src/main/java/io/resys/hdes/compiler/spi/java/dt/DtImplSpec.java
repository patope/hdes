package io.resys.hdes.compiler.spi.java.dt;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.DecisionTableMeta;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableMetaEntry;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableDecisionTableMeta;
import io.resys.hdes.executor.api.ImmutableExecution;

public class DtImplSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }
  
  public static class Builder {
    private final Namings namings;
    private final List<AnnotationSpec> annotations = Arrays.asList(
        AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
        AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtImplSpec.class.getCanonicalName()).build());
    private final MethodSpec sourceType = MethodSpec.methodBuilder("getSourceType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(SourceType.class)
        .addStatement("return $T.DT", SourceType.class)
        .build();
    private final MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
        .addStatement("this.when = when")
        .build();
    
    private DecisionTableBody body;
    
    public Builder(Namings namings) {
      super();
      this.namings = namings;
    }
    
    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final ClassName outputName = namings.dt().outputValueMono(body);
      final ClassName immutableOutputName = JavaSpecUtil.immutable(outputName);
      
      CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("long start = System.currentTimeMillis()")
          .addStatement("int id = 0")
          .addStatement("$T<Integer, $T> meta = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class)
          .addStatement("$T.Builder result = $T.builder()", immutableOutputName, immutableOutputName);
      
      if(body.getHitPolicy() instanceof HitPolicyFirst) {
        execution.add(HitPolicyFirstSpec.builder(namings).body(body).build());
      } else if(body.getHitPolicy() instanceof HitPolicyAll) {
        execution.add(HitPolicyAllSpec.builder(namings).body(body).build());
      } else  {
        execution.add(HitPolicyMatrixSpec.builder(namings).body(body).build());
      }
      
      
      execution.add("\r\n")
      .addStatement("long end = System.currentTimeMillis()")
      .add("$T metaWrapper = $T.builder()", DecisionTableMeta.class, ImmutableDecisionTableMeta.class)
      .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
      .add("\r\n  ").add(".start(start).end(end).time(end - start)")
      .add("\r\n  ").addStatement(".values(meta).build()", body.getId().getValue(), ExecutionStatus.class)
      
      .addStatement("$T.Builder<$T, $T> resultWrapper = $T.builder()", ImmutableExecution.class, DecisionTableMeta.class, outputName, ImmutableExecution.class)
      .addStatement("return resultWrapper.meta(metaWrapper).value(result.build()).build()")
      .build();
      
      
      /* Create formula on input
      DtFormulaSpec formula = visitFormula(body.getHeaders());
      if(!formula.getInputs().isEmpty()) {
        statements.addStatement("input = $L(input)", APPLY_INPUT_FORMULA);
      } */
      
      
       
      /* Create formula on input
      DtFormulaSpec formula = visitFormula(body.getHeaders());
      if(!formula.getInputs().isEmpty()) {
        statements.addStatement("input = $L(input)", APPLY_INPUT_FORMULA);
      }*/
      
      
      return TypeSpec.classBuilder(namings.dt().impl(body))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.dt().api(body))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotations(annotations)
          .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
          .addMethod(constructor)
          .addMethod(sourceType)
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(namings.dt().inputValue(body), "input").build())
              .returns(namings.dt().execution(body))
              .addCode(execution.build())
              .build())
          .build();
    }
  }
}
