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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.CompilerContext;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;

public class HitPolicyMatrixSpec {

  public static Builder builder(CompilerContext namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    @SuppressWarnings("unused")
    private final CompilerContext namings;
    private DecisionTableBody body;

    public Builder(CompilerContext namings) {
      super();
      this.namings = namings;
    }

    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }

    public CodeBlock build() {
      Assertions.notNull(body, () -> "body must be defined!");
      
      HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
      CodeBlock.Builder execution = CodeBlock.builder();
      for (MatrixRow matrixRow : matrix.getRows()) {
        execution.add("// row $L \r\n", matrixRow.getTypeName().getValue());

        int index = 0;
        for (Literal literal : matrixRow.getValues()) {

          ScalarDef header = (ScalarDef) body.getHeaders().getValues().stream()
              .filter(t -> t.getName().equals(matrixRow.getTypeName().getValue())).findFirst().get();
          Rule rule = matrix.getRules().get(index);

          CodeBlock valueToSet = DtRuleSpec.builder(body).build(header, literal).getValue();
          CodeBlock expression = DtRuleSpec.builder(body).build(header, rule).getValue();

          var prefix = index > 0 ? "else " : "";
          execution.beginControlFlow(prefix + "if($L)", expression)
              .add("meta.put(id, $T.builder()", ImmutableDecisionTableMetaEntry.class)
              .add(".id(id++)")
              .add(".index($L)", index++).add(".token($L)", DtTokenSpec.build(literal, "not available"))
              .addStatement(".build())").addStatement("result.$L($L)", header.getName(), valueToSet)
              .endControlFlow();

        }
        execution.add("\r\n");
      }

      return execution.build();
    }

  }

}
