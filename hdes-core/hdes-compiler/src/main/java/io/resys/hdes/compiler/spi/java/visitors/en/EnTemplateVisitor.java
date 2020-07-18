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
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.ExpressionAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AdditiveOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ConditionalExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.ExpressionBody;
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

public class EnTemplateVisitor<R, T> implements ExpressionAstNodeVisitor<R, T> {
  @Override
  public R visitTypeName(TypeName node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitExpressionBody(ExpressionBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitNotUnaryOperation(NotUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitNegateUnaryOperation(NegateUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitMethodRefNode(MethodRefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }
  
  @Override
  public R visitEqualityOperation(EqualityOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitAndOperation(AndOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitOrOperation(OrOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitConditionalExpression(ConditionalExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitBetweenExpression(BetweenExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitAdditiveOperation(AdditiveOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitMultiplicativeOperation(MultiplicativeOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
