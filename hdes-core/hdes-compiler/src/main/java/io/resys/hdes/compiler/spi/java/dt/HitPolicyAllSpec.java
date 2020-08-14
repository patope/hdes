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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.dt.RuleRowSpec.DtControlStatement;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;

public class HitPolicyAllSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    private final Namings namings;
    private DecisionTableBody body;

    public Builder(Namings namings) {
      super();
      this.namings = namings;
    }

    public Builder body(DecisionTableBody body) {
      this.body = body;
      return this;
    }
    
    public CodeBlock build() {
      Assertions.notNull(body, () -> "body must be defined!");
      CodeBlock.Builder execution = CodeBlock.builder();
      
      // parse all rules
      int rowIndex = 0;
      HitPolicyAll all = (HitPolicyAll) body.getHitPolicy();
      for (RuleRow row : all.getRows()) {
        DtControlStatement pair = RuleRowSpec.builder(namings).body(body).build(row);
        CodeBlock value = CodeBlock.builder()
            .add("Immutable$T.builder()$L.build()", namings.dt().outputValueFlux(body), pair.getValue()).build();
        
        execution.add("\r\n");
        
        // control start
        if (!pair.getControl().isEmpty()) {
          execution.beginControlFlow("if($L)", pair.getControl()).add("\r\n");
        }

        execution
        .add("meta.put(id, $T.builder()", ImmutableDecisionTableMetaEntry.class)
        .add(".id(id++)")
        .add(".index($L)", rowIndex++)
        .add(".token($L)", DtTokenSpec.build(row, row.getText()))
        .addStatement(".build())")
        .addStatement("result.addValues($L)", value);
        
        // Control end
        if (!pair.getControl().isEmpty()) {
          execution.endControlFlow();
        } 
      }
      
      return execution.build();
    }
  }
}
