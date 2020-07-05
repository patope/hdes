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
import io.resys.hdes.compiler.spi.java.visitors.en.EnJavaSpec.EnRefSpec;

public class EnTemplateVisitor implements ExpressionAstNodeVisitor<EnRefSpec, TypeSpec> {
  @Override
  public EnRefSpec visitTypeName(TypeName node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public TypeSpec visitExpressionBody(ExpressionBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitNotUnaryOperation(NotUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitMethodRefNode(MethodRefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }
  
  @Override
  public EnRefSpec visitEqualityOperation(EqualityOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitAndOperation(AndOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitOrOperation(OrOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitConditionalExpression(ConditionalExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitBetweenExpression(BetweenExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitAdditiveOperation(AdditiveOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public EnRefSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
