package io.resys.hdes.compiler.spi.dt;

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.lang.model.element.Modifier;

import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicy;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.CompilerContext;
import io.resys.hdes.compiler.spi.dt.RuleRowSpec.DtControlStatement;
import io.resys.hdes.compiler.spi.en.ExpressionSpec;
import io.resys.hdes.compiler.spi.en.ExpressionVisitor.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.invocation.InvocationGetMethod;
import io.resys.hdes.compiler.spi.invocation.InvocationGetMethodDt;
import io.resys.hdes.compiler.spi.invocation.InvocationSpec;
import io.resys.hdes.compiler.spi.invocation.InvocationSpec.InvocationType;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.executor.api.DecisionTableMeta.DecisionTableMetaEntry;
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.HdesWhen;
import io.resys.hdes.executor.api.ImmutableDecisionTableMeta;

public class DtImplSpec {
  
  public static Builder builder(CompilerContext namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }
  
  public static class Builder {
    private final CompilerContext namings;
    private final List<AnnotationSpec> annotations = Arrays.asList(
        AnnotationSpec.builder(SuppressWarnings.class).addMember("value", "$S", "unused").build(),
        AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtImplSpec.class.getCanonicalName()).build());
    private final MethodSpec sourceType = MethodSpec.methodBuilder("getSourceType")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(SourceType.class)
        .addStatement("return $T.$L", SourceType.class, SourceType.DT)
        .build();
    private final MethodSpec constructor = MethodSpec.constructorBuilder()
        .addModifiers(Modifier.PUBLIC)
        .addParameter(ParameterSpec.builder(HdesWhen.class, "when").build())
        .addStatement("this.when = when")
        .build();
    
    private DecisionTableBody body;
    
    public Builder(CompilerContext namings) {
      super();
      this.namings = namings;
    }
    
    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }

    private CodeBlock formula(ScalarDef scalarDef) {
      ClassName typeName = scalarDef.getDirection() == DirectionType.IN ? 
          namings.dt().inputValue(body) :
          namings.dt().outputValueMono(body);
      
      EnScalarCodeSpec formulaSpec = ExpressionSpec.builder().envir(namings.ast()).parent(body).dtf(scalarDef);
      return CodeBlock.builder()
          .addStatement("mutator = $T.builder().from(mutator).$L($L).build()", JavaSpecUtil.immutable(typeName), scalarDef.getName(), formulaSpec.getValue())
          .build();
    }
    
    private Optional<MethodSpec> inputFormula(DecisionTableBody body) {
      // Create formula on input
      final ClassName inputType = namings.dt().inputValue(body);
      
      List<CodeBlock> inputFormulas = body.getHeaders().getValues().stream()
        .filter(v -> v.getDirection() == DirectionType.IN).map(v -> (ScalarDef) v)
        .filter(v -> v.getFormula().isPresent()).map(v -> formula(v))
        .collect(Collectors.toList());
      
      if(inputFormulas.isEmpty()) {
        return Optional.empty();
      }
      
      CodeBlock.Builder builder = CodeBlock.builder()
          .addStatement("$T mutator = $L", inputType, InvocationGetMethod.ACCESS_INPUT_VALUE);
      
      for(CodeBlock codeBlock : inputFormulas) {
        builder.add(codeBlock).add("\r\n");
      }
      
      return Optional.of(MethodSpec
        .methodBuilder("applyInputFormula")
        .addModifiers(Modifier.PUBLIC)
        .addCode(builder.addStatement("return mutator").build())
        .addParameter(inputType, InvocationGetMethod.ACCESS_INPUT_VALUE)
        .returns(inputType)
        .build());
    }
    
    private Optional<MethodSpec> outputFormula(DecisionTableBody body) {
      // Create formula on output
      List<CodeBlock> outputFormulas = body.getHeaders().getValues().stream()
        .filter(v -> v.getDirection() == DirectionType.OUT).map(v -> (ScalarDef) v)
        .filter(v -> v.getFormula().isPresent()).map(v -> formula(v))
        .collect(Collectors.toList());
      if(outputFormulas.isEmpty()) {
        return Optional.empty();
      }  
      
      final ClassName inputType = namings.dt().inputValue(body);
      final ClassName outputType = namings.dt().outputValueMono(body);
      
      CodeBlock.Builder builder = CodeBlock.builder()
          .addStatement("$T mutator = $L", outputType, InvocationGetMethodDt.ACCESS_OUTPUT_VALUE);
      
      for(CodeBlock codeBlock : outputFormulas) {
        builder.add(codeBlock).add("\r\n");
      }
      
      return Optional.of(MethodSpec
        .methodBuilder("applyOutputFormula")
        .addModifiers(Modifier.PUBLIC)
        .addCode(builder.addStatement("return mutator").build())
        .addParameter(inputType, InvocationGetMethod.ACCESS_INPUT_VALUE)
        .addParameter(outputType, InvocationGetMethodDt.ACCESS_OUTPUT_VALUE)
        .returns(outputType)
        .build());
    }
    
    private Optional<CodeBlock> staticValue(DecisionTableBody body) {
      boolean isStaticUsagePresent = body.getHeaders().getValues().stream()
        .map(h -> (ScalarDef) h)
        .filter(h -> h.getFormula().isPresent())
        .map(h -> InvocationSpec.builder().envir(namings.ast()).parent(body).build(h.getFormula().get()))
        .map(h -> h.getTypes().contains(InvocationType.STATIC))
        .filter(v -> v)
        .findFirst().orElse(false);
      
      if(!isStaticUsagePresent) {
        return Optional.empty();
      }
      
      ClassName outputType = JavaSpecUtil.immutable(namings.dt().outputValueFlux(body));
      final ClassName staticType = namings.dt().staticValue(body);
      final ClassName immutableStaticType = JavaSpecUtil.immutable(staticType);
      CodeBlock.Builder execution = CodeBlock.builder().add("$T.builder()", immutableStaticType);
      
      HitPolicy hitPolicy = body.getHitPolicy();
      if(hitPolicy instanceof HitPolicyFirst || hitPolicy instanceof HitPolicyAll) {
        
        List<RuleRow> rows = hitPolicy instanceof HitPolicyFirst ? 
            ((HitPolicyFirst) hitPolicy).getRows() : 
            ((HitPolicyAll) hitPolicy).getRows();
          
        for (RuleRow row : rows) {
          DtControlStatement pair = RuleRowSpec.builder(namings).body(body).build(row);
          
          execution.add("\r\n").add(".addValues($T.builder()$L.build())", outputType, pair.getValue());
          
        }

        execution.add("\r\n").add(".build()");
      } else  {
        HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
        for (MatrixRow matrixRow : matrix.getRows()) {

          ScalarDef header = (ScalarDef) body.getHeaders().getValues().stream()
              .filter(t -> t.getName().equals(matrixRow.getTypeName().getValue())).findFirst().get();
          
          CodeBlock.Builder values = CodeBlock.builder(); 
          for (Literal literal : matrixRow.getValues()) {
            if(!values.isEmpty()) {
              values.add(", ");
            }
            values.add("$L", DtRuleSpec.builder(body).build(header, literal).getValue());
          }
          execution.add("\r\n").add(".$L($T.asList($L))", header.getName(), Arrays.class, values.build());
          execution.add("\r\n").add(".addValues($T.asList($L))", Arrays.class, values.build());
        }
        execution.add("\r\n").add(".build()");
      }
      
      return Optional.of(execution.build());
    }
    
    public TypeSpec build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      final List<MethodSpec> formulas = new ArrayList<>();
      final ClassName staticType = namings.dt().staticValue(body);
      final ClassName inputType = namings.dt().inputValue(body);
      final ClassName outputType = namings.dt().outputValueMono(body);
      final ClassName immutableOutputType = JavaSpecUtil.immutable(outputType);
      
      CodeBlock.Builder execution = CodeBlock.builder()
          .addStatement("long start = System.currentTimeMillis()")
          .addStatement("int id = 0")
          .addStatement("$T<Integer, $T> meta = new $T<>()", Map.class, DecisionTableMetaEntry.class, HashMap.class)
          .addStatement("$T.Builder result = $T.builder()", immutableOutputType, immutableOutputType);
      
      // init static
      Optional<CodeBlock> staticValue = staticValue(body);
      if(staticValue.isPresent()) {
        execution.addStatement("$T staticValue = $L", namings.dt().staticValue(body), staticValue.get()).add("\r\n");
      }
      
      // Create formula on input
      Optional<MethodSpec> inputFormula = inputFormula(body);
      if(inputFormula.isPresent()) {
        if(staticValue.isPresent()) {
          formulas.add(inputFormula.get().toBuilder().addParameter(staticType, "staticValue").build());
          execution.addStatement("input = $L(input, staticValue)", inputFormula.get().name);
        } else {
          formulas.add(inputFormula.get());
          execution.addStatement("input = $L(input)", inputFormula.get().name);
        }
      }
      
      if(body.getHitPolicy() instanceof HitPolicyFirst) {
        execution.add(HitPolicyFirstSpec.builder(namings).body(body).build());
      } else if(body.getHitPolicy() instanceof HitPolicyAll) {
        execution.add(HitPolicyAllSpec.builder(namings).body(body).build());
      } else  {
        execution.add(HitPolicyMatrixSpec.builder(namings).body(body).build());
      }
      
      execution.addStatement("$T output = result.build()", outputType);
      
      // Create formula on output
      Optional<MethodSpec> outputFormula = outputFormula(body);
      if(outputFormula.isPresent()) {
        if(staticValue.isPresent()) {
          formulas.add(outputFormula.get().toBuilder().addParameter(staticType, "staticValue").build());
          execution.addStatement("output = $L(input, output, staticValue)", outputFormula.get().name);
        } else {
          formulas.add(outputFormula.get());
          execution.addStatement("output = $L(input, output)", outputFormula.get().name);
        }
      }
      
      CodeBlock metaValue = CodeBlock.builder()
          .add("$T.builder()", ImmutableDecisionTableMeta.class)
          .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
          .add("\r\n  ").add(".start(start).end(end).time(end - start)")
          .add("\r\n  ").add(".values(meta).build()").build();
      
      execution.add("\r\n")
      .addStatement("long end = System.currentTimeMillis()")
      .addStatement("return execution(input, output, $L)", metaValue)
      .build();
    
      return TypeSpec.classBuilder(namings.dt().impl(body))
          .addModifiers(Modifier.PUBLIC)
          .addSuperinterface(namings.dt().api(body))
          .superclass(namings.dt().template(body))
          .addJavadoc(body.getDescription().orElse(""))
          .addAnnotations(annotations)
          .addField(FieldSpec.builder(HdesWhen.class, "when", Modifier.PRIVATE, Modifier.FINAL).build())
          .addMethod(constructor)
          .addMethod(sourceType)
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(inputType, "input").build())
              .returns(namings.dt().execution(body))
              .addCode(execution.build())
              .build())
          .addMethods(formulas)
          .build();
    }
  }
}