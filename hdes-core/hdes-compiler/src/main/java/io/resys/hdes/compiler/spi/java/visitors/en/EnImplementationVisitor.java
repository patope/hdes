package io.resys.hdes.compiler.spi.java.visitors.en;

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

import com.squareup.javapoet.CodeBlock;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.ArrayTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarType;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityType;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
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
import io.resys.hdes.compiler.spi.java.visitors.JavaSpecUtil;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnCodeSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnConvertionSpec;
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.TypeNameResolver;

public class EnImplementationVisitor extends EnTemplateVisitor<EnCodeSpec, EnCodeSpec> implements ExpressionAstNodeVisitor<EnCodeSpec, EnCodeSpec> {
  
  private final TypeNameResolver resolver;
  
  public EnImplementationVisitor(TypeNameResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public EnCodeSpec visitExpressionBody(ExpressionBody node) {
    return visit(node.getValue());
  }
  
  @Override
  public EnCodeSpec visitTypeName(TypeName node) {
    TypeDefNode typeDefNode = resolver.accept(node);
    if(!(typeDefNode instanceof ScalarTypeDefNode)) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleScalarType(node, typeDefNode));
    }
    ScalarTypeDefNode scalarNode = (ScalarTypeDefNode) typeDefNode;
    return ImmutableEnCodeSpec.builder()
        .value(CodeBlock.builder()
          .add("input.")
          .add(JavaSpecUtil.methodCall(node.getValue()))
          .build())
        .type(scalarNode.getType())
        .build();
  }

  @Override
  public EnCodeSpec visitLiteral(Literal node) {
    CodeBlock.Builder builder = CodeBlock.builder();
    switch (node.getType()) {
    case BOOLEAN:
      builder.add(node.getValue()); break;
    case DATE:
      builder.add("$T.parse($S)", LocalDate.class, node.getValue()); break;
    case DATE_TIME:
      builder.add("$T.parse($S)", LocalDateTime.class, node.getValue()); break;
    case TIME:
      builder.add("$T.parse($S)", LocalTime.class, node.getValue()); break;
    case DECIMAL:
      builder.add("new $T($S)", BigDecimal.class, node.getValue()); break;
    case INTEGER:
      builder.add("$L", node.getValue()); break;
    case STRING:
      builder.add("$S", node.getValue()); break;
    default:
      throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(node));
    }
    
    return ImmutableEnCodeSpec.builder()
        .value(builder.build())
        .type(node.getType())
        .build();
  }

  @Override
  public EnCodeSpec visitNotUnaryOperation(NotUnaryOperation node) {
    EnCodeSpec children = visit(node.getValue());
    
    if(children.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, children.getType()));
    }
    
    return ImmutableEnCodeSpec.builder()
        .type(ScalarType.BOOLEAN)
        .value(CodeBlock.builder().add("!").add(children.getValue()).build())
        .build();
  }

  @Override
  public EnCodeSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    EnCodeSpec spec = visit(node.getValue());

    CodeBlock.Builder value = CodeBlock.builder();
    if(spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.negate()", spec.getValue());
    } else if(spec.getType() == ScalarType.INTEGER) {      
      value.add("-$L", spec.getValue());
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleTypesInNegateUnaryOperation(node, spec.getType())); 
    }
    
    return ImmutableEnCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  @Override
  public EnCodeSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    EnCodeSpec spec = visit(node.getValue());

    CodeBlock.Builder value = CodeBlock.builder();
    if(spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.plus()", spec.getValue());
    } else if(spec.getType() == ScalarType.INTEGER) {      
      value.add("+$L", spec.getValue());
    } else {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleTypesInPlusUnaryOperation(node, spec.getType())); 
    }
    
    return ImmutableEnCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }

  /*
  @Override
  public EnCodeSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    // TODO
    return visit(node.getValue());
  }

  @Override
  public EnCodeSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    // TODO
    return visit(node.getValue());
  }

  @Override
  public EnCodeSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    // TODO
    return visit(node.getValue());
  }

  @Override
  public EnCodeSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    // TODO
    return visit(node.getValue());
  }*/
  
  @Override
  public EnCodeSpec visitMethodRefNode(MethodRefNode node) {
    
    // global functions: min, max, sum, avg
    if(node.getType().isEmpty()) {
      
      EnJavaSpec.listConverter();
      for(AstNode ast : node.getValues()) {
        if(ast instanceof ArrayTypeDefNode) {
          
        } else if(ast instanceof ObjectTypeDefNode) {
          
        }
        
        EnCodeSpec runningValue = visit(ast);
      }
      
      throw new HdesCompilerException(HdesCompilerException.builder().unknownGlobalFunctionCall(node, node.getName(), "min, max, sum, avg"));
    }
    
    
    return ImmutableEnCodeSpec.builder()
        
        .build();
  }
  
  @Override
  public EnCodeSpec visitEqualityOperation(EqualityOperation node) {

    EnConvertionSpec betweenSpec = EnJavaSpec.converter()
        .src(node)
        .value1(visit(node.getLeft()))
        .value2(visit(node.getRight()))
        .build();
    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();
    
    CodeBlock.Builder body = CodeBlock.builder();
    switch (commonType) {
    case DECIMAL:
      if(node.getType() == EqualityType.EQUAL) {
        body.add("$L.compareTo($L) == 0", left, right);
      } else if(node.getType() == EqualityType.LESS) {
        body.add("$L.compareTo($L) < 0", left, right);  
      } else if(node.getType() == EqualityType.LESS_THEN) {
        body.add("$L.compareTo($L) <= 0", left, right);
      } else if(node.getType() == EqualityType.GREATER) {
        body.add("$L.compareTo($L) > 0", left, right);
      } else if(node.getType() == EqualityType.GREATER_THEN) {
        body.add("$L.compareTo($L) >= 0", left, right);
      }
      break;
    case INTEGER:
      if(node.getType() == EqualityType.EQUAL) {
        body.add("Integer.compare($L, $L) == 0", left, right);
      } else if(node.getType() == EqualityType.LESS) {
        body.add("Integer.compare($L, $L) < 0", left, right);  
      } else if(node.getType() == EqualityType.LESS_THEN) {
        body.add("Integer.compare($L, $L) <= 0", left, right);
      } else if(node.getType() == EqualityType.GREATER) {
        body.add("Integer.compare($L, $L) > 0", left, right);
      } else if(node.getType() == EqualityType.GREATER_THEN) {
        body.add("Integer.compare($L, $L) >= 0", left, right);
      }
      break;
    case DATE_TIME:
    case DATE:
    case TIME:
      if(node.getType() == EqualityType.EQUAL) {
        body.add("$L.isEqual($L)", left, right);
      } else if(node.getType() == EqualityType.LESS) {
        body.add("$L.isBefore($L)", left, right);  
      } else if(node.getType() == EqualityType.LESS_THEN) {
        body.add("($L.isBefore($L) || $L.isEqual($L))", left, right, left, right);
      } else if(node.getType() == EqualityType.GREATER) {
        body.add("$L.isAfter($L)", left, right);
      } else if(node.getType() == EqualityType.GREATER_THEN) {
        body.add("($L.isAfter($L) || $L.isEqual($L))", left, right, left, right);
      }
      break;
    default:
      throw new HdesCompilerException(HdesCompilerException.builder().betweenOperationNotSupportedForType(node, commonType));
    }
    return ImmutableEnCodeSpec.builder()
        .value(body.build())
        .type(commonType)
        .build();
  
  }
  
  @Override
  public EnCodeSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    
    EnCodeSpec left = visit(node.getLeft());
    EnCodeSpec right = visit(node.getRight());
    EnConvertionSpec spec = EnJavaSpec.converter().src(node).value1(left).value2(right).build();
    
    if(spec.getType() != ScalarType.INTEGER && spec.getType() != ScalarType.DECIMAL) {
      throw new HdesCompilerException(HdesCompilerException.builder()
          .incompatibleTypesInAdditiveOperation(node, left.getType(), right.getType()));
    }

    CodeBlock.Builder value = CodeBlock.builder();
    if(spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", 
          spec.getValue1(),
          node.getType() == MultiplicativeType.MULTIPLY ? "multiply" : "divide",
          spec.getValue2());
    } else if(node.getType() == MultiplicativeType.MULTIPLY) {
      value.add("$L * $L", spec.getValue1(), spec.getValue2());
    } else {
      value.add("new $T($L).divide(new $T($L)))", BigDecimal.class, spec.getValue1(), BigDecimal.class, spec.getValue2());      
    }
    
    return ImmutableEnCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }
  
  @Override
  public EnCodeSpec visitAdditiveOperation(AdditiveOperation node) {
    
    EnCodeSpec left = visit(node.getLeft());
    EnCodeSpec right = visit(node.getRight());
    EnConvertionSpec spec = EnJavaSpec.converter().src(node).value1(left).value2(right).build();
    
    if(spec.getType() != ScalarType.INTEGER && spec.getType() != ScalarType.DECIMAL) {
      throw new HdesCompilerException(HdesCompilerException.builder()
          .incompatibleTypesInAdditiveOperation(node, left.getType(), right.getType()));
    }

    CodeBlock.Builder value = CodeBlock.builder();
    if(spec.getType() == ScalarType.DECIMAL) {
      value.add("$L.$L($L)", 
          spec.getValue1(),
          node.getType() == AdditiveType.ADD ? "add" : "subtract",
          spec.getValue2());
    } else {
      value.add("$L $L $L", 
          spec.getValue1(),
          node.getType() == AdditiveType.ADD ? "+" : "-",
          spec.getValue2());
    }

    return ImmutableEnCodeSpec.builder().value(value.build()).type(spec.getType()).build();
  }
  
  @Override
  public EnCodeSpec visitAndOperation(AndOperation node) {
    EnCodeSpec left = visit(node.getLeft());
    if(left.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, left.getType()));
    }

    EnCodeSpec right = visit(node.getLeft());
    if(right.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, right.getType()));
    }

    return ImmutableEnCodeSpec.builder()
        .value(CodeBlock.builder().add(left.getValue()).add(" && ").add(right.getValue()).build())
        .build();
  }

  @Override
  public EnCodeSpec visitOrOperation(OrOperation node) {
    EnCodeSpec left = visit(node.getLeft());
    if(left.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, left.getType()));
    }

    EnCodeSpec right = visit(node.getLeft());
    if(right.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, right.getType()));
    }

    return ImmutableEnCodeSpec.builder()
        .value(CodeBlock.builder().add(left.getValue()).add(" || ").add(right.getValue()).build())
        .build();
  }

  @Override
  public EnCodeSpec visitConditionalExpression(ConditionalExpression node) {
    EnCodeSpec condition = visit(node.getOperation());
    if(condition.getType() != ScalarType.BOOLEAN) {
      throw new HdesCompilerException(HdesCompilerException.builder().incompatibleType(node, ScalarType.BOOLEAN, condition.getType()));
    }
    EnConvertionSpec conversion = EnJavaSpec.converter()
        .src(node)
        .value1(visit(node.getLeft()))
        .value2(visit(node.getRight()))
        .build();
    
    return ImmutableEnCodeSpec.builder()
        .value(CodeBlock.builder()
            .add(condition.getValue()).add("?")
            .add(conversion.getValue1())
            .add(":")
            .add(conversion.getValue2())
            .build())
        .type(conversion.getType())
        .build();
  }

  @Override
  public EnCodeSpec visitBetweenExpression(BetweenExpression node) {
    
    EnConvertionSpec betweenSpec = EnJavaSpec.converter()
        .src(node)
        .value1(visit(node.getLeft()))
        .value2(visit(node.getRight()))
        .build();
    EnCodeSpec valueSpec = visit(node.getValue());
    
    ScalarType commonType = betweenSpec.getType();
    CodeBlock left = betweenSpec.getValue1();
    CodeBlock right = betweenSpec.getValue2();
    CodeBlock value = valueSpec.getValue();
    
    if(valueSpec.getType() != betweenSpec.getType()) {
      EnConvertionSpec conversion1 = EnJavaSpec.converter()
          .src(node)
          .value1(valueSpec)
          .value2(ImmutableEnCodeSpec.builder().type(betweenSpec.getType()).value(left).build())
          .build();
      
      // new types
      value = conversion1.getValue1();
      left = conversion1.getValue2();
      
      EnConvertionSpec conversion2 = EnJavaSpec.converter()
          .src(node)
          .value1(ImmutableEnCodeSpec.builder().type(conversion1.getType()).value(value).build())
          .value2(ImmutableEnCodeSpec.builder().type(betweenSpec.getType()).value(right).build())
          .build();

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
      throw new HdesCompilerException(HdesCompilerException.builder().betweenOperationNotSupportedForType(node, commonType));
    }
    
    //lte(left, arg) && gte(right, arg)
    
    return ImmutableEnCodeSpec.builder()
        .value(CodeBlock.builder()
            .add(leftBuilder.build())
            .add(" && ")
            .add(rightBuilder.build())
            .build())
        .type(commonType)
        .build();
  }

  private EnCodeSpec visit(AstNode node) {
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
    } else if (node instanceof PreIncrementUnaryOperation) {
      return visitPreIncrementUnaryOperation((PreIncrementUnaryOperation) node);
    } else if (node instanceof PreDecrementUnaryOperation) {
      return visitPreDecrementUnaryOperation((PreDecrementUnaryOperation) node);
    } else if (node instanceof PostIncrementUnaryOperation) {
      return visitPostIncrementUnaryOperation((PostIncrementUnaryOperation) node);
    } else if (node instanceof PostDecrementUnaryOperation) {
      return visitPostDecrementUnaryOperation((PostDecrementUnaryOperation) node);
    } else if (node instanceof MethodRefNode) {
      return visitMethodRefNode((MethodRefNode) node);
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
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
