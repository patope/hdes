package io.resys.hdes.compiler.spi.java.dt;

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
import io.resys.hdes.executor.api.HdesExecutable.ExecutionStatus;
import io.resys.hdes.executor.api.HdesExecutable.SourceType;
import io.resys.hdes.executor.api.ImmutableFormulaMeta;

public class DtFrImplSpec {

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
      EnScalarCodeSpec formulaSpec = ExpressionSpec.builder().envir(namings.ast()).parent(body).dtf(formula);
      if(formula.getArray() != formulaSpec.getArray()) {
        throw new HdesCompilerException(HdesCompilerException.builder().dtFormulaContainsIncorectArrayType(formula, formulaSpec.getArray()));
      }
      
      if(formula.getType() != formulaSpec.getType()) {
        throw new HdesCompilerException(HdesCompilerException.builder().dtFormulaContainsIncorectScalarTypes(formula, formulaSpec.getType()));
      }
      
      ClassName inputType = namings.fr().inputValue(body, formula);
      
      CodeBlock metaValue = CodeBlock.builder()
        .add("$T.builder()", ImmutableFormulaMeta.class)
        .add("\r\n  ").add(".id($S).status($T.COMPLETED) ", body.getId().getValue(), ExecutionStatus.class)
        .add("\r\n  ").add(".start(start).end(end).time(end - start)")
        .add("\r\n  ").add(".build()")
        .add("\r\n").build();
      
      CodeBlock.Builder execution = CodeBlock.builder()
      .addStatement("long start = System.currentTimeMillis()")
      .addStatement("var result = $L", formulaSpec.getValue())
      .addStatement("long end = System.currentTimeMillis()")
      .addStatement("return execution($L, $L, $L)", 
          ExpressionVisitor.ACCESS_SRC_VALUE, 
          CodeBlock.builder().add("$T.builder().$L(result).build()", JavaSpecUtil.immutable(outputType), formula.getName()).build(),
          metaValue
      );
      
      return TypeSpec.classBuilder(namings.fr().impl(body, formula))
          .addModifiers(Modifier.PUBLIC)
          .addAnnotation(AnnotationSpec.builder(javax.annotation.processing.Generated.class).addMember("value", "$S", DtFrImplSpec.class.getCanonicalName()).build())
          .superclass(namings.fr().template(body, formula))
          .addSuperinterface(namings.fr().api(body, formula))
          .addMethod(MethodSpec.methodBuilder("getSourceType")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .returns(SourceType.class)
              .addStatement("return $T.$L", SourceType.class, SourceType.FR)
              .build())
          
          .addMethod(MethodSpec.methodBuilder("apply")
              .addAnnotation(Override.class)
              .addModifiers(Modifier.PUBLIC)
              .addParameter(ParameterSpec.builder(inputType, ExpressionVisitor.ACCESS_SRC_VALUE).build())
              .returns(namings.fr().execution(body, formula))
              .addCode(execution.build())
              .build())
          .build();
    }
  }
}
