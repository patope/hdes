package io.resys.hdes.compiler.spi.java.visitors.en;

import com.squareup.javapoet.TypeSpec;

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
import io.resys.hdes.ast.api.nodes.ExpressionNode.TypeRefNode;

public class EnTemplateVisitor implements ExpressionAstNodeVisitor<EnJavaSpec, TypeSpec> {
  @Override
  public EnJavaSpec visitTypeName(TypeName node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public TypeSpec visitExpressionBody(ExpressionBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitNotUnaryOperation(NotUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
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
  public EnJavaSpec visitMethodRefNode(MethodRefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitTypeRefNode(TypeRefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitEqualityOperation(EqualityOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitAndOperation(AndOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitOrOperation(OrOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitConditionalExpression(ConditionalExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitBetweenExpression(BetweenExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitAdditiveOperation(AdditiveOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnJavaSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
