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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderIndex;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.InOperation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.NegateLiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.spi.Assertions;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.TypeConverter;
import io.resys.hdes.compiler.spi.java.en.TypeConverter.EnConvertionSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;

public class DtRuleSpec {
  public final static String HEADER_REF = "//header ref to be replaces";

  @Value.Immutable
  public interface DtExpressionCodeSpec {
    CodeBlock getValue();
    ScalarType getType();
  }

  public static Builder builder(DecisionTableBody body) {
    Assertions.notNull(body, () -> "body must be defined!");
    return new Builder(body);
  }

  public static class Builder {
    private final DecisionTableBody body;
    private TypeDef header;
    
    private Builder(DecisionTableBody body) {
      super();
      this.body = body;
    }


    public DtExpressionCodeSpec build(TypeDef header, AstNode node) {
      Assertions.notNull(node, () -> "node must be defined!");
      Assertions.notNull(header, () -> "header must be defined!");
      this.header = header;
      return accept(node);
    }

    private DtExpressionCodeSpec rule(Rule rule) {
      RuleValue value = rule.getValue();
      String getMethod = JavaSpecUtil.methodName(header.getName());

      // optional type
      if (!header.getRequired()) {
        getMethod = getMethod + "()" + ".get";
      }

      if (value instanceof LiteralValue) {

        Literal literal = ((LiteralValue) value).getValue();
        CodeBlock literalCode = accept(literal).getValue();
        CodeBlock.Builder exp = CodeBlock.builder();

        if (literal.getType() == ScalarType.DECIMAL) {
          exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
        } else if (literal.getType() == ScalarType.DATE) {
          exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
        } else if (literal.getType() == ScalarType.DATE_TIME) {
          exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
        } else if (literal.getType() == ScalarType.TIME) {
          exp.add("input.$L().compareTo($L) == 0", getMethod, literalCode);
        } else if (literal.getType() == ScalarType.STRING) {
          exp.add("input.$L().equals($L)", getMethod, literalCode);
        } else {
          exp.add("input.$L() == $L", getMethod, literalCode);
        }
        return ImmutableDtExpressionCodeSpec.builder().value(exp.build()).type(ScalarType.BOOLEAN).build();

      } else if (value instanceof ExpressionValue) {
        ExpressionValue expressionValue = (ExpressionValue) value;
        DtExpressionCodeSpec spec = accept(expressionValue.getExpression());
        String inputName = CodeBlock.builder().add("input.$L()", getMethod).build().toString();

        return ImmutableDtExpressionCodeSpec.builder()
            .value(CodeBlock.builder().add(spec.getValue().toString().replaceAll(HEADER_REF, inputName)).build())
            .type(spec.getType()).build();
        
      } else if (value instanceof UndefinedValue) {
        return ImmutableDtExpressionCodeSpec.builder()
            .value(CodeBlock.builder().add("true").build())
            .type(ScalarType.BOOLEAN).build(); 
      }
      
      throw new HdesCompilerException(HdesCompilerException.builder().unknownDTInputRule(rule));
    }
    
    private DtExpressionCodeSpec equality(EqualityOperation node) {
      DtExpressionCodeSpec value1 = accept(node.getLeft());
      DtExpressionCodeSpec value2 = accept(node.getRight());

      EnConvertionSpec spec = new TypeConverter().src(node).value1(value1.getValue(), value1.getType())
          .value2(value2.getValue(), value2.getType()).build();
      CodeBlock left = spec.getValue1();
      CodeBlock right = spec.getValue2();

      String operation;
      switch (node.getType()) {
      case EQUAL:
        operation = "$L.eq($L, $L)";
        break;
      case NOTEQUAL:
        operation = "$L.neq($L, $L)";
        break;
      case GREATER:
        operation = "$L.gt($L, $L)";
        break;
      case GREATER_THEN:
        operation = "$L.gte($L, $L)";
        break;
      case LESS:
        operation = "$L.lt($L, $L)";
        break;
      case LESS_THEN:
        operation = "$L.lte($L, $L)";
        break;
      default:
        throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionOperation(node));
      }
      return ImmutableDtExpressionCodeSpec.builder().type(ScalarType.BOOLEAN)
          .value(CodeBlock.builder().add(operation, "when", left, right).build()).build();
    }

    private DtExpressionCodeSpec in(InOperation node) {
      StringBuilder values = new StringBuilder();
      for (Literal literal : node.getValues()) {
        if (values.length() > 0) {
          values.append(", ");
        }
        values.append(literal(literal).getValue().toString());
      }
      
      return ImmutableDtExpressionCodeSpec.builder()
          .value(CodeBlock.builder().add("when.asList($L).contains($L)", values.toString(), HEADER_REF).build())
          .type(ScalarType.BOOLEAN).build();
    }

    private DtExpressionCodeSpec not(NotUnary node) {
      CodeBlock child = accept(node.getValue()).getValue();
      return ImmutableDtExpressionCodeSpec.builder().value(CodeBlock.builder().add("!").add(child).build())
          .type(ScalarType.BOOLEAN).build();
    }

    private DtExpressionCodeSpec headerRef(HeaderIndex node) {
      ScalarDef header = (ScalarDef) body.getHeaders().getValues().get(node.getIndex());
      return ImmutableDtExpressionCodeSpec.builder().value(CodeBlock.builder().add(HEADER_REF).build())
          .type(header.getType()).build();
    }

    private DtExpressionCodeSpec between(BetweenExpression node) {
      CodeBlock value = accept(node.getValue()).getValue();
      CodeBlock left = accept(node.getLeft()).getValue();
      CodeBlock right = accept(node.getRight()).getValue();
      return ImmutableDtExpressionCodeSpec.builder()
          .value(CodeBlock.builder().add("when.between($L, $L, $L)", value, left, right).build())
          .type(ScalarType.BOOLEAN).build();
    }

    private DtExpressionCodeSpec and(AndExpression node) {
      CodeBlock left = accept(node.getLeft()).getValue();
      CodeBlock right = accept(node.getRight()).getValue();
      return ImmutableDtExpressionCodeSpec.builder()
          .value(CodeBlock.builder().add(left).add("\r\n  && ").add(right).build()).type(ScalarType.BOOLEAN).build();
    }

    private DtExpressionCodeSpec literal(Literal node) {
      CodeBlock.Builder code = CodeBlock.builder();
      if (node.getType() == ScalarType.DECIMAL) {
        code.add("new $T(\"$L\")", BigDecimal.class, node.getValue());
      } else if (node.getType() == ScalarType.DATE) {
        code.add("$T.parse($L)", LocalDate.class, node.getValue());
      } else if (node.getType() == ScalarType.DATE_TIME) {
        code.add("$T.parse($L)", LocalDateTime.class, node.getValue());
      } else if (node.getType() == ScalarType.TIME) {
        code.add("$T.parse($L)", LocalTime.class, node.getValue());
      } else if (node.getType() == ScalarType.STRING) {
        code.add("$S", node.getValue());
      } else {
        code.add(node.getValue());
      }
      return ImmutableDtExpressionCodeSpec.builder().value(code.build()).type(node.getType()).build();
    }

    private DtExpressionCodeSpec negateLiteral(NegateLiteralValue negate) {
      Literal node = negate.getValue();
      CodeBlock.Builder code = CodeBlock.builder();
      if (node.getType() == ScalarType.DECIMAL) {
        code.add("new $T(\"-$L\")", BigDecimal.class, node.getValue());
      } else if (node.getType() == ScalarType.INTEGER) {
        code.add("-$L", node.getValue());
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionOperation(node));
      }
      return ImmutableDtExpressionCodeSpec.builder().value(code.build()).type(node.getType()).build();
    }

    private DtExpressionCodeSpec accept(AstNode node) {
      if (node instanceof Literal) {
        return literal((Literal) node);
      } else if (node instanceof HeaderIndex) {
        return headerRef((HeaderIndex) node);
      } else if (node instanceof InOperation) {
        return in((InOperation) node);
      } else if (node instanceof NotUnary) {
        return not((NotUnary) node);
      } else if (node instanceof EqualityOperation) {
        return equality((EqualityOperation) node);
      } else if (node instanceof AndExpression) {
        return and((AndExpression) node);
      } else if (node instanceof BetweenExpression) {
        return between((BetweenExpression) node);
      } else if (node instanceof NegateLiteralValue) {
        return negateLiteral((NegateLiteralValue) node);
      } else if(node instanceof Rule) {
        return rule((Rule) node);
      }
      throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
    }
  }
}
