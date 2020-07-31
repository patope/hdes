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

import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
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

public class EnTemplateVisitor<R, T> implements ExpressionAstNodeVisitor<R, T> {
  @Override
  public R visitTypeInvocation(TypeInvocation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitBody(ExpressionBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitNot(NotUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitNegate(NegateUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPositive(PositiveUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPreIncrement(PreIncrementUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPreDecrement(PreDecrementUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPostIncrement(PostIncrementUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPostDecrement(PostDecrementUnary node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitMethod(MethodInvocation node) {
    throw new IllegalArgumentException("Not implemented");
  }
  
  @Override
  public R visitEquality(EqualityOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitAnd(AndExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitOr(OrExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitConditional(ConditionalExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitBetween(BetweenExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitAdditive(AdditiveExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitMultiplicative(MultiplicativeExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitLambda(LambdaExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
