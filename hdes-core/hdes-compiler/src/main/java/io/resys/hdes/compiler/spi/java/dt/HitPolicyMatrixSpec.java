package io.resys.hdes.compiler.spi.java.dt;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.Namings;
import io.resys.hdes.executor.api.ImmutableDecisionTableMetaEntry;

public class HitPolicyMatrixSpec {

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
      
      HitPolicyMatrix matrix = (HitPolicyMatrix) body.getHitPolicy();
      CodeBlock.Builder execution = CodeBlock.builder();
      for (MatrixRow matrixRow : matrix.getRows()) {
        execution.add("// row $L \r\n", matrixRow.getTypeName().getValue());

        int index = 0;
        for (Literal literal : matrixRow.getValues()) {

          ScalarTypeDefNode header = (ScalarTypeDefNode) body.getHeaders().getValues().stream()
              .filter(t -> t.getName().equals(matrixRow.getTypeName().getValue())).findFirst().get();
          Rule rule = matrix.getRules().get(index);

          CodeBlock valueToSet = DtRuleSpec.builder(body).build(header, literal).getValue();
          CodeBlock expression = DtRuleSpec.builder(body).build(header, rule).getValue();

          var prefix = index > 0 ? "else " : "";
          execution.beginControlFlow(prefix + "if($L)", expression)
              .add("meta.put(id, $T.builder()", ImmutableDecisionTableMetaEntry.class).add("\r\n  .id(id++)")
              .add("\r\n  .index($L)", index++).add("\r\n  .token($L)", DtTokenSpec.build(literal, "not available"))
              .addStatement(".build())").addStatement("result.$L($L)", header.getName(), valueToSet)
              .endControlFlow();

        }
        execution.add("\r\n");
      }

      return execution.build();
    }

  }

}
