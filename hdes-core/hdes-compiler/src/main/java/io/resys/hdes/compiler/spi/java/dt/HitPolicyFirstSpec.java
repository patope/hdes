package io.resys.hdes.compiler.spi.java.dt;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.java.visitors.dt.DtJavaSpec;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;

public class HitPolicyFirstSpec {

  @Value.Immutable
  public interface DtFirstControlStatement extends DtJavaSpec {
    CodeBlock getControl();
    CodeBlock getValue();
  }
  
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
    
    private DtFirstControlStatement ruleRow(RuleRow node) {
      CodeBlock.Builder key = CodeBlock.builder();
      CodeBlock.Builder value = CodeBlock.builder().add("result");
      boolean and = false;
      for (Rule rule : node.getRules()) {        
        RuleValue ruleValue = rule.getValue();
        if (ruleValue instanceof UndefinedValue) {
          continue;
        }

        final ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().get(rule.getHeader());

        if (header.getDirection() == DirectionType.IN) {
          CodeBlock ruleCode = DtRuleSpec.builder(body).build(header, rule).getValue();
          if(ruleCode.toString().equals("true")) {
            continue;
          }
          
          if (and) {
            key.add("\r\n  && ");
          }
          key.add(ruleCode);
          and = true;
          
        } else {
          CodeBlock literal = DtRuleSpec.builder(body).build(header, ((LiteralValue) rule.getValue()).getValue()).getValue();
          CodeBlock ruleCode = CodeBlock.builder().add(".$L($L)", header.getName(), literal).build();
          if (ruleCode.isEmpty()) {
            continue;
          }
          
          value.add(ruleCode);
        }
      }
      return ImmutableDtFirstControlStatement.builder()
          .control(key.build()).value(value.build())
          .build();
    }
    
    public CodeBlock build() {
      Assertions.notNull(body, () -> "body must be defined!");
      CodeBlock.Builder execution = CodeBlock.builder();
      int rowIndex = 0;
      
      HitPolicyFirst first = (HitPolicyFirst) body.getHitPolicy();
      
      for (RuleRow row : first.getRows()) {
        DtFirstControlStatement pair = ruleRow(row);

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
        .addStatement(pair.getValue());
        
        // Control end
        execution.add("\r\n").endControlFlow();
         
      }
      return execution.build();
    }
  }
}
