package io.resys.hdes.compiler.spi.java.en;

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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.immutables.value.Value;

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.DirectionType;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodRefNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnaryOperation;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnJavaSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnScalarCodeSpec;
import io.resys.hdes.compiler.spi.java.en.TypeConverter.EnConvertionSpec;
import io.resys.hdes.compiler.spi.naming.JavaSpecUtil;
import io.resys.hdes.executor.spi.HdesMath;

public class ExpressionVisitor implements ExpressionAstNodeVisitor<EnJavaSpec, EnScalarCodeSpec> {
  public static String ACCESS_INPUT_VALUE = "inputValue";
  public static String ACCESS_OUTPUT_VALUE = "outputValue";
  public static String ACCESS_STATIC_VALUE = "staticValue";
  public static String ACCESS_INSTANCE_VALUE = "instanceValue";
  public static String ACCESS_SRC_VALUE = "src";
  
  private final static List<String> GLOBAL_METHODS = Arrays.asList("min", "max", "sum", "avg");
  private final EnReferedTypeResolver resolver;
  public interface EnJavaSpec { }

  @Value.Immutable
  public interface EnRefSpec extends EnJavaSpec {
    List<AstNode> getValues();
  }

  @Value.Immutable
  public interface EnScalarCodeSpec extends EnJavaSpec {
    CodeBlock getValue();
    Optional<Boolean> getArray();
    ScalarType getType();
  }

  @Value.Immutable
  public interface EnObjectCodeSpec extends EnJavaSpec {
    CodeBlock getValue();
    Optional<Boolean> getArray();
    ObjectTypeDefNode getType();
  }

  public ExpressionVisitor(EnReferedTypeResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public EnScalarCodeSpec visitExpressionBody(ExpressionBody node) {
    return visitScalar(node.getValue());
  }

  @Override
  public EnScalarCodeSpec visitTypeName(TypeName node) {
    TypeDefNode typeDefNode = resolver.accept(node);
    if (!(typeDefNode instanceof ScalarTypeDefNode)) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleScalarType(node, typeDefNode));
    }
    final String scope;
    if(node.getScope() == TypeNameScope.STATIC) {
      scope = ACCESS_STATIC_VALUE;
    } else if(node.getScope() == TypeNameScope.INSTANCE) {
      scope = ACCESS_INSTANCE_VALUE;
    } else if(typeDefNode.getDirection() == DirectionType.IN) {
      scope = ACCESS_INPUT_VALUE;
    } else {
      scope = ACCESS_OUTPUT_VALUE;
    }
    String name = scope + "." + typeDefNode.getName();
    ScalarTypeDefNode scalarNode = (ScalarTypeDefNode) typeDefNode;
    return ImmutableEnScalarCodeSpec.builder()
        .value(CodeBlock.builder().add("$L.$L", ACCESS_SRC_VALUE, 
            JavaSpecUtil.methodCall(name) + (typeDefNode.getRequired() ? "" : ".get()")
            ).build())
        .type(scalarNode.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitLiteral(Literal node) {
    CodeBlock.Builder builder = CodeBlock.builder();
    switch (node.getType()) {
    case BOOLEAN:
      builder.add(node.getValue());
      break;
    case DATE:
      builder.add("$T.parse($S)", LocalDate.class, node.getValue());
      break;
    case DATE_TIME:
      builder.add("$T.parse($S)", LocalDateTime.class, node.getValue());
      break;
    case TIME:
      builder.add("$T.parse($S)", LocalTime.class, node.getValue());
      break;
    case DECIMAL:
      builder.add("new $T($S)", BigDecimal.class, node.getValue());
      break;
    case INTEGER:
      builder.add("$L", node.getValue());
      break;
    case STRING:
      builder.add("$S", node.getValue());
      break;
    default:
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(node));
    }

    return ImmutableEnScalarCodeSpec.builder().value(builder.build()).type(node.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitNotUnaryOperation(NotUnaryOperation node) {
    EnScalarCodeSpec children = visitScalar(node.getValue());

    if (children.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, children.getType()));
    }

    return ImmutableEnScalarCodeSpec.builder().type(ScalarType.BOOLEAN)
        .value(CodeBlock.builder().add("!").add(children.getValue()).build()).build();
  }

  @Override
  public EnScalarCodeSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    EnScalarCodeSpec spec = visitScalar(node.getValue());

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.negate()", spec.getValue());
    } else if (spec.getType() == ScalarType.INTEGER) {
      value.add("-$L", spec.getValue());
    } else {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleTypesInNegateUnaryOperation(node, spec.getType()));
    }

    return ImmutableEnScalarCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    EnScalarCodeSpec spec = visitScalar(node.getValue());

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.plus()", spec.getValue());
    } else if (spec.getType() == ScalarType.INTEGER) {
      value.add("+$L", spec.getValue());
    } else {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleTypesInPlusUnaryOperation(node, spec.getType()));
    }

    return ImmutableEnScalarCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public EnJavaSpec visitMethodRefNode(MethodRefNode node) {

    if (node.getType().isEmpty()) {

      if (!GLOBAL_METHODS.contains(node.getName())) {
        throw new HdesCompilerException(
            HdesCompilerException.builder().unknownGlobalFunctionCall(node, node.getName(), "min, max, sum, avg"));
      }

      boolean isDecimal = false;
      CodeBlock.Builder params = CodeBlock.builder().add("$T.builder()", HdesMath.class);
      for (AstNode ast : node.getValues()) {
        EnJavaSpec spec = visitAny(ast);

        // Scalar ref
        if (spec instanceof EnScalarCodeSpec) {
          EnScalarCodeSpec runningValue = (EnScalarCodeSpec) spec;
          if (runningValue.getType() == ScalarType.INTEGER) {
            params.add(".integer($L)", runningValue.getValue());
          } else if (runningValue.getType() == ScalarType.DECIMAL) {
            params.add(".decimal($L)", runningValue.getValue());
            isDecimal = true;
          }
          continue;
        }

        // Object based ref
        EnObjectCodeSpec runningValue = (EnObjectCodeSpec) spec;
        for (TypeDefNode typeDefNode : runningValue.getType().getValues()) {
          if (typeDefNode instanceof ScalarTypeDefNode) {
            continue;
          }
          ScalarType scalar = ((ScalarTypeDefNode) typeDefNode).getType();
          String name = JavaSpecUtil.methodCall(typeDefNode.getName());
          if (scalar == ScalarType.INTEGER) {
            params.add(".integer($L.$L)", runningValue.getValue(), name);
          } else if (scalar == ScalarType.DECIMAL) {
            params.add(".decimal($L.$L)", runningValue.getValue(), name);
            isDecimal = true;
          }
        }
      }

      if (isDecimal) {
        params.add(".toDecimal()");
      } else {
        params.add(".toInteger()");
      }

      ScalarType returnType = isDecimal || node.getName().equals("avg") ? ScalarType.DECIMAL : ScalarType.INTEGER;
      return ImmutableEnScalarCodeSpec.builder().value(params.add(".$L()", node.getName()).build()).array(false)
          .type(returnType).build();
    }

    TypeDefNode typeDef = this.resolver.accept(node);
    if (typeDef instanceof ScalarTypeDefNode) {
      return ImmutableEnScalarCodeSpec.builder()
          .value(CodeBlock.builder().add("input.").add(JavaSpecUtil.methodCall(node.getName())).build())
          .array(typeDef.getArray()).type(((ScalarTypeDefNode) typeDef).getType()).build();

    } else {
      return ImmutableEnObjectCodeSpec.builder()
          .value(CodeBlock.builder().add("input.").add(JavaSpecUtil.methodCall(node.getName())).build())
          .array(typeDef.getArray()).type((ObjectTypeDefNode) typeDef).build();

    }
  }

  @Override
  public EnScalarCodeSpec visitEqualityOperation(EqualityOperation node) {

    EnConvertionSpec betweenSpec = new TypeConverter().src(node).value1(visitScalar(node.getLeft()))
        .value2(visitScalar(node.getRight())).build();
    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();

    CodeBlock.Builder body = CodeBlock.builder();
    switch (commonType) {
    case DECIMAL:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("$L.compareTo($L) == 0", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("$L.compareTo($L) < 0", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("$L.compareTo($L) <= 0", left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("$L.compareTo($L) > 0", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("$L.compareTo($L) >= 0", left, right);
      }
      break;
    case INTEGER:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("Integer.compare($L, $L) == 0", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("Integer.compare($L, $L) < 0", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("Integer.compare($L, $L) <= 0", left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("Integer.compare($L, $L) > 0", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("Integer.compare($L, $L) >= 0", left, right);
      }
      break;
    case DATE_TIME:
    case DATE:
    case TIME:
      if (node.getType() == EqualityType.EQUAL) {
        body.add("$L.isEqual($L)", left, right);
      } else if (node.getType() == EqualityType.LESS) {
        body.add("$L.isBefore($L)", left, right);
      } else if (node.getType() == EqualityType.LESS_THEN) {
        body.add("($L.isBefore($L) || $L.isEqual($L))", left, right, left, right);
      } else if (node.getType() == EqualityType.GREATER) {
        body.add("$L.isAfter($L)", left, right);
      } else if (node.getType() == EqualityType.GREATER_THEN) {
        body.add("($L.isAfter($L) || $L.isEqual($L))", left, right, left, right);
      }
      break;
    default:
      throw new HdesCompilerException(
          HdesCompilerException.builder().betweenOperationNotSupportedForType(node, commonType));
    }
    return ImmutableEnScalarCodeSpec.builder().value(body.build()).type(commonType).build();

  }

  @Override
  public EnScalarCodeSpec visitMultiplicativeOperation(MultiplicativeOperation node) {

    EnScalarCodeSpec left = visitScalar(node.getLeft());
    EnScalarCodeSpec right = visitScalar(node.getRight());
    EnConvertionSpec spec = new TypeConverter().src(node).value1(left).value2(right).build();

    if (spec.getType() != ScalarType.INTEGER && spec.getType() != ScalarType.DECIMAL) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleTypesInAdditiveOperation(node, left.getType(), right.getType()));
    }

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", spec.getValue1(), node.getType() == MultiplicativeType.MULTIPLY ? "multiply" : "divide",
          spec.getValue2());
    } else if (node.getType() == MultiplicativeType.MULTIPLY) {
      value.add("$L * $L", spec.getValue1(), spec.getValue2());
    } else {
      value.add("new $T($L).divide(new $T($L)))", BigDecimal.class, spec.getValue1(), BigDecimal.class,
          spec.getValue2());
    }

    return ImmutableEnScalarCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitAdditiveOperation(AdditiveOperation node) {

    EnScalarCodeSpec left = visitScalar(node.getLeft());
    EnScalarCodeSpec right = visitScalar(node.getRight());
    EnConvertionSpec spec = new TypeConverter().src(node).value1(left).value2(right).build();

    if (spec.getType() != ScalarType.INTEGER && spec.getType() != ScalarType.DECIMAL) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleTypesInAdditiveOperation(node, left.getType(), right.getType()));
    }

    CodeBlock.Builder value = CodeBlock.builder();
    if (spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", spec.getValue1(), node.getType() == AdditiveType.ADD ? "add" : "subtract",
          spec.getValue2());
    } else {
      value.add("$L $L $L", spec.getValue1(), node.getType() == AdditiveType.ADD ? "+" : "-", spec.getValue2());
    }

    return ImmutableEnScalarCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitAndOperation(AndOperation node) {
    EnScalarCodeSpec left = visitScalar(node.getLeft());
    if (left.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, left.getType()));
    }

    EnScalarCodeSpec right = visitScalar(node.getLeft());
    if (right.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, right.getType()));
    }

    return ImmutableEnScalarCodeSpec.builder()
        .value(CodeBlock.builder().add(left.getValue()).add(" && ").add(right.getValue()).build()).build();
  }

  @Override
  public EnScalarCodeSpec visitOrOperation(OrOperation node) {
    EnScalarCodeSpec left = visitScalar(node.getLeft());
    if (left.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, left.getType()));
    }

    EnScalarCodeSpec right = visitScalar(node.getLeft());
    if (right.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, right.getType()));
    }

    return ImmutableEnScalarCodeSpec.builder()
        .value(CodeBlock.builder().add(left.getValue()).add(" || ").add(right.getValue()).build()).build();
  }

  @Override
  public EnScalarCodeSpec visitConditionalExpression(ConditionalExpression node) {
    EnScalarCodeSpec condition = visitScalar(node.getOperation());
    if (condition.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(
          HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, condition.getType()));
    }
    EnConvertionSpec conversion = new TypeConverter().src(node).value1(visitScalar(node.getLeft()))
        .value2(visitScalar(node.getRight())).build();

    return ImmutableEnScalarCodeSpec.builder().value(CodeBlock.builder().add(condition.getValue()).add("?")
        .add(conversion.getValue1()).add(":").add(conversion.getValue2()).build()).type(conversion.getType()).build();
  }

  @Override
  public EnScalarCodeSpec visitBetweenExpression(BetweenExpression node) {

    EnConvertionSpec betweenSpec = new TypeConverter().src(node).value1(visitScalar(node.getLeft()))
        .value2(visitScalar(node.getRight())).build();
    EnScalarCodeSpec valueSpec = visitScalar(node.getValue());

    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();
    CodeBlock value = valueSpec.getValue();

    if (valueSpec.getType() != betweenSpec.getType()) {
      EnConvertionSpec conversion1 = new TypeConverter().src(node).value1(valueSpec)
          .value2(ImmutableEnScalarCodeSpec.builder().type(betweenSpec.getType()).value(left).build()).build();

      // new types
      value = conversion1.getValue1();
      left = conversion1.getValue2();

      EnConvertionSpec conversion2 = new TypeConverter().src(node)
          .value1(ImmutableEnScalarCodeSpec.builder().type(conversion1.getType()).value(value).build())
          .value2(ImmutableEnScalarCodeSpec.builder().type(betweenSpec.getType()).value(right).build()).build();

      // new types
      commonType = conversion2.getType();
      right = conversion2.getValue2();
    }

    CodeBlock.Builder leftBuilder = CodeBlock.builder();
    CodeBlock.Builder rightBuilder = CodeBlock.builder();
    switch (commonType) {
    case DECIMAL:
      leftBuilder.add("$L.compareTo($L) <= 0", left, value);
      rightBuilder.add("$L.compareTo($L) >= 0", right, value);
      break;
    case INTEGER:
      leftBuilder.add("Integer.compareTo($L, $L) <= 0", left, value);
      rightBuilder.add("Integer.compareTo($L, $L) >= 0", right, value);
      break;
    case DATE_TIME:
    case DATE:
    case TIME:
      leftBuilder.add("($L.isBefore($L) || $L.isEqual($L))", left, value, left, value);
      rightBuilder.add("($L.isAfter($L) || $L.isEqual($L))", right, value, right, value);
      break;
    default:
      throw new HdesCompilerException(
          HdesCompilerException.builder().betweenOperationNotSupportedForType(node, commonType));
    }

    // lte(left, arg) && gte(right, arg)

    return ImmutableEnScalarCodeSpec.builder()
        .value(CodeBlock.builder().add(leftBuilder.build()).add(" && ").add(rightBuilder.build()).build())
        .type(commonType).build();
  }

  private EnScalarCodeSpec visitScalar(AstNode node) {
    EnJavaSpec result = visitAny(node);
    if (result instanceof EnScalarCodeSpec) {
      return (EnScalarCodeSpec) result;
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }

  private EnJavaSpec visitAny(AstNode node) {
    if (node instanceof TypeName) {
      return visitTypeName((TypeName) node);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node);
    } else if (node instanceof NotUnaryOperation) {
      return visitNotUnaryOperation((NotUnaryOperation) node);
    } else if (node instanceof NegateUnaryOperation) {
      return visitNegateUnaryOperation((NegateUnaryOperation) node);
    } else if (node instanceof PositiveUnaryOperation) {
      return visitPositiveUnaryOperation((PositiveUnaryOperation) node);
    } else if (node instanceof EqualityOperation) {
      return visitEqualityOperation((EqualityOperation) node);
    } else if (node instanceof AndOperation) {
      return visitAndOperation((AndOperation) node);
    } else if (node instanceof OrOperation) {
      return visitOrOperation((OrOperation) node);
    } else if (node instanceof ConditionalExpression) {
      return visitConditionalExpression((ConditionalExpression) node);
    } else if (node instanceof BetweenExpression) {
      return visitBetweenExpression((BetweenExpression) node);
    } else if (node instanceof AdditiveOperation) {
      return visitAdditiveOperation((AdditiveOperation) node);
    } else if (node instanceof MultiplicativeOperation) {
      return visitMultiplicativeOperation((MultiplicativeOperation) node);
    } else if (node instanceof MethodRefNode) {
      return visitMethodRefNode((MethodRefNode) node);
    } else if (node instanceof PreIncrementUnaryOperation) {
      return visitPreIncrementUnaryOperation((PreIncrementUnaryOperation) node);
    } else if (node instanceof PreDecrementUnaryOperation) {
      return visitPreDecrementUnaryOperation((PreDecrementUnaryOperation) node);
    } else if (node instanceof PostIncrementUnaryOperation) {
      return visitPostIncrementUnaryOperation((PostIncrementUnaryOperation) node);
    } else if (node instanceof PostDecrementUnaryOperation) {
      return visitPostDecrementUnaryOperation((PostDecrementUnaryOperation) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }

  @Override
  public EnJavaSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitLambdaExpression(LambdaExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
