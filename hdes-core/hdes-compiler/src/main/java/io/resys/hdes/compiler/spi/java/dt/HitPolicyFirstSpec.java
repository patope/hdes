package io.resys.hdes.compiler.spi.java.dt;

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
            .add("\r\n  .id(id)")
            .add("\r\n  .index($L)", rowIndex++)
            .add("\r\n  .token($L)", DtTokenSpec.build(row, row.getText()))
            .add("\r\n  .build()")
            .build())
        .addStatement(value);
        
        // Control end
        execution.add("\r\n").endControlFlow();
         
      }
      return execution.build();
    }
  }
}
