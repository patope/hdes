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
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.AstNode.TypeNameScope;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
import io.resys.hdes.ast.api.nodes.ExpressionNode.LambdaExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MethodInvocation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.MultiplicativeExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NegateUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PositiveUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PostIncrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreDecrementUnary;
import io.resys.hdes.ast.api.nodes.ExpressionNode.PreIncrementUnary;
import io.resys.hdes.compiler.api.HdesCompilerException;
import io.resys.hdes.compiler.spi.java.en.ExpressionRefsSpec.EnReferedTypeResolver;
import io.resys.hdes.compiler.spi.java.en.ExpressionVisitor.EnRefSpec;
import io.resys.hdes.compiler.spi.java.en.ImmutableEnRefSpec;

public class EnInterfaceVisitor extends EnTemplateVisitor<EnRefSpec, List<TypeDef>> implements ExpressionAstNodeVisitor<EnRefSpec, List<TypeDef>> {
  
  private final EnReferedTypeResolver resolver;
  
  public EnInterfaceVisitor(EnReferedTypeResolver resolver) {
    super();
    this.resolver = resolver;
  }

  @Override
  public List<TypeDef> visitBody(ExpressionBody node) {
    List<TypeDef> result = new ArrayList<>();
    for(AstNode ref : visit(node.getValue()).getValues()) {
      if(ref instanceof TypeInvocation) {
        TypeInvocation typeName = (TypeInvocation) ref;
        if(typeName.getScope() == TypeNameScope.STATIC) {
          continue;
        }
        
        TypeDef def = resolver.accept(typeName); 
        result.add(def);
      } else if(ref instanceof MethodInvocation) {
        EnRefSpec spec = visitMethod((MethodInvocation) ref);
        
        for(AstNode child : spec.getValues()) {
          if(child instanceof TypeDef) {
            result.add((TypeDef) child);
          }
        }
      } else {
        throw new HdesCompilerException(HdesCompilerException.builder().unknownExpressionParameter(ref));
      }
    }
    return result;
  }
  
  @Override
  public EnRefSpec visitLambda(LambdaExpression node) {
    List<String> lambdaParams = node.getParams().stream()
        .map(t -> t.getValue())
        .collect(Collectors.toList());
    
    List<AstNode> values = visit(node.getBody()).getValues().stream()
      .filter(t -> t instanceof TypeInvocation)
      .map(t -> (TypeInvocation) t)
      .filter(t -> !lambdaParams.contains(t.getValue()))
      .collect(Collectors.toList());

    return ImmutableEnRefSpec.builder().addAllValues(values).build();
  }
  
  @Override
  public EnRefSpec visitTypeInvocation(TypeInvocation node) {
    return ImmutableEnRefSpec.builder().addValues(node).build();
  }

  @Override
  public EnRefSpec visitMethod(MethodInvocation node) {
    List<AstNode> values = new ArrayList<>();
    if(node.getType().isPresent()) {
      values.addAll(visitTypeInvocation(node.getType().get()).getValues());
    }
    node.getValues().forEach(v -> values.addAll(visit(v).getValues()));
    return ImmutableEnRefSpec.builder().addAllValues(values).build();
  }
  
  @Override
  public EnRefSpec visitLiteral(Literal node) {
    return ImmutableEnRefSpec.builder().build();
  }

  @Override
  public EnRefSpec visitNot(NotUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitNegate(NegateUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPositive(PositiveUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreIncrement(PreIncrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPreDecrement(PreDecrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostIncrement(PostIncrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitPostDecrement(PostDecrementUnary node) {
    return visit(node.getValue());
  }

  @Override
  public EnRefSpec visitEquality(EqualityOperation node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAnd(AndExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitOr(OrExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitConditional(ConditionalExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitBetween(BetweenExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .addAllValues(visit(node.getValue()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitAdditive(AdditiveExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  @Override
  public EnRefSpec visitMultiplicative(MultiplicativeExpression node) {
    return ImmutableEnRefSpec.builder()
        .addAllValues(visit(node.getLeft()).getValues())
        .addAllValues(visit(node.getRight()).getValues())
        .build();
  }

  private EnRefSpec visit(AstNode node) {
    if (node instanceof TypeInvocation) {
      return visitTypeInvocation((TypeInvocation) node);
    } else if (node instanceof Literal) {
      return visitLiteral((Literal) node);
    } else if (node instanceof NotUnary) {
      return visitNot((NotUnary) node);
    } else if (node instanceof NegateUnary) {
      return visitNegate((NegateUnary) node);
    } else if (node instanceof PositiveUnary) {
      return visitPositive((PositiveUnary) node);
    } else if (node instanceof PreIncrementUnary) {
      return visitPreIncrement((PreIncrementUnary) node);
    } else if (node instanceof PreDecrementUnary) {
      return visitPreDecrement((PreDecrementUnary) node);
    } else if (node instanceof PostIncrementUnary) {
      return visitPostIncrement((PostIncrementUnary) node);
    } else if (node instanceof PostDecrementUnary) {
      return visitPostDecrement((PostDecrementUnary) node);
    } else if (node instanceof MethodInvocation) {
      return visitMethod((MethodInvocation) node);
    } else if (node instanceof EqualityOperation) {
      return visitEquality((EqualityOperation) node);
    } else if (node instanceof AndExpression) {
      return visitAnd((AndExpression) node);
    } else if (node instanceof OrExpression) {
      return visitOr((OrExpression) node);
    } else if (node instanceof ConditionalExpression) {
      return visitConditional((ConditionalExpression) node);
    } else if (node instanceof BetweenExpression) {
      return visitBetween((BetweenExpression) node);
    } else if (node instanceof AdditiveExpression) {
      return visitAdditive((AdditiveExpression) node);
    } else if (node instanceof MultiplicativeExpression) {
      return visitMultiplicative((MultiplicativeExpression) node);
    } else if(node instanceof LambdaExpression) {
      return visitLambda((LambdaExpression) node);
    }
    throw new HdesCompilerException(HdesCompilerException.builder().unknownDTExpressionNode(node));
  }
}
