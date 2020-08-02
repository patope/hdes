package io.resys.hdes.compiler.spi.java.dt;

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
        .add("\r\n  .id(id++)")
        .add("\r\n  .index($L)", rowIndex++)
        .add("\r\n  .token($L)", DtTokenSpec.build(row, row.getText()))
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
