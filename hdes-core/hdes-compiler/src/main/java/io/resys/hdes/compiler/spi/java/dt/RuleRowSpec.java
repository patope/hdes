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

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.spi.naming.Namings;

public class RuleRowSpec {

  @Value.Immutable
  public interface DtControlStatement {
    CodeBlock getControl();
    CodeBlock getValue();
  }
  
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
    
    public DtControlStatement build(RuleRow node) {
      Assertions.notNull(body, () -> "body must be defined!");
      Assertions.notNull(node, () -> "node must be defined!");
      CodeBlock.Builder key = CodeBlock.builder();
      CodeBlock.Builder value = CodeBlock.builder();
      
      boolean and = false;
      for (Rule rule : node.getRules()) {        
        RuleValue ruleValue = rule.getValue();
        if (ruleValue instanceof UndefinedValue) {
          continue;
        }

        final ScalarDef header = (ScalarDef) body.getHeaders().getValues().get(rule.getHeader());

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
      return ImmutableDtControlStatement.builder()
          .control(key.build()).value(value.build())
          .build();
    }
  }
}
