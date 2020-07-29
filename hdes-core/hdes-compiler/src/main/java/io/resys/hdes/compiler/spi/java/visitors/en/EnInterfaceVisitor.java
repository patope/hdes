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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import io.resys.hdes.ast.api.nodes.AstNode;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodRefNode;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnaryOperation;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnRefSpec;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.ImmutableEnRefSpec;

public class EnInterfaceVisitor extends EnTemplateVisitor<EnRefSpec, List<TypeDefNode>> implements ExpressionAstNodeVisitor<EnRefSpec, List<TypeDefNode>> {
  
  private final EnReferedTypeResolver resolver;
  
  public EnInterfaceVisitor(EnReferedTypeResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public List<TypeDefNode> visitExpressionBody(ExpressionBody node) {
    List<TypeDefNode> result = new ArrayList<>();
    for(AstNode ref : visit(node.getValue()).getValues()) {
      if(ref instanceof TypeName) {
        TypeName typeName = (TypeName) ref;
        if(typeName.getScope() == TypeNameScope.STATIC) {
          continue;
        }
        
        TypeDefNode def = resolver.accept(typeName); 
        result.add(def);
      } else if(ref instanceof MethodRefNode) {
        EnRefSpec spec = visitMethodRefNode((MethodRefNode) ref);
        
        for(AstNode child : spec.getValues()) {
          if(child instanceof TypeDefNode) {
            result.add((TypeDefNode) child);
          }
        }
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(ref));
      }
    }
    return result;
  }
  
  @Override
  public EnRefSpec visitLambdaExpression(LambdaExpression node) {
    List<String> lambdaParams = node.getParams().stream()
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    List<AstNode> values = visit(node.getBody()).getValues().stream()
      .filter(t -> t instanceof TypeName)
      .map(t -> (TypeName) t)
      .filter(t -> !lambdaParams.contains(t.getValue()))
      .collect(Collectors.toList());

    return ImmutableEnRefSpec.builder().addAllValues(values).build();
  }
  
  @Override
  public EnRefSpec visitTypeName(TypeName node) {
    return ImmutableEnRefSpec.builder().addValues(node).build();
  }

  @Override
  public EnRefSpec visitMethodRefNode(MethodRefNode node) {
    List<AstNode> values = new ArrayList<>();
    if(node.getType().isPresent()) {
      values.addAll(visitTypeName(node.getType().get()).getValues());
    }
    node.getValues().forEach(v -> values.addAll(visit(v).getValues()));
    return ImmutableEnRefSpec.builder().addAllValues(values).build();
  }
  
  @Override
  public EnRefSpec visitLiteral(Literal node) {
    return ImmutableEnRefSpec.builder().build();
  }

  @Override
  public EnRefSpec visitNotUnaryOperation(NotUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitEqualityOperation(EqualityOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAndOperation(AndOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitOrOperation(OrOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitConditionalExpression(ConditionalExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitBetweenExpression(BetweenExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .addAllValues(visit(node.getValue()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAdditiveOperation(AdditiveOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  private EnRefSpec visit(AstNode node) {
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
    } else if(node instanceof LambdaExpression) {
      return visitLambdaExpression((LambdaExpression) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
