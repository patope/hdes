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
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.dt.RuleRowSpec.DtControlStatement;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;

public class HitPolicyFirstSpec {
  
  public static Builder builder(Namings namings) {
    Assertions.notNull(namings, () -> "namings must be defined!");
    return new Builder(namings);
  }

  public static class Builder {
    @SuppressWarnings("unused")
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
      int rowIndex = 0;
      
      HitPolicyFirst first = (HitPolicyFirst) body.getHitPolicy();
      
      for (RuleRow row : first.getRows()) {
        DtControlStatement pair = RuleRowSpec.builder(namings).body(body).build(row);
        CodeBlock value = CodeBlock.builder().add("result$L", pair.getValue()).build();
        
        // control start        
        String elseControl = rowIndex > 0 ? "else " : "";
        String control = pair.getControl().isEmpty() ? "true" : pair.getControl().toString();
        execution.beginControlFlow(elseControl + "if($L)", control).add("\r\n");
        
        execution
        .addStatement("meta.put(0, $L)", CodeBlock.builder()
            .add("$T.builder()", ImmutableDecisionTableMetaEntry.class)
            .add(".id(id)")
            .add(".index($L)", rowIndex++)
            .add(".token($L)", DtTokenSpec.build(row, row.getText()))
            .add(".build()")
            .build())
        .addStatement(value);
        
        // Control end
        execution.add("\r\n").endControlFlow();
         
      }
      return execution.build();
    }
  }
}
