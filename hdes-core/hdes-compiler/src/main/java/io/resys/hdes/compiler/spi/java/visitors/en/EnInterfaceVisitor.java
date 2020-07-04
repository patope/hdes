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

public class EnInterfaceVisitor implements ExpressionAstNodeVisitor<EnJavaSpec, TypeSpec> {

  @Override
  public EnJavaSpec visitTypeName(TypeName node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitLiteral(Literal node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public TypeSpec visitExpressionBody(ExpressionBody node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitNotUnaryOperation(NotUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitNegateUnaryOperation(NegateUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitPositiveUnaryOperation(PositiveUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitPreIncrementUnaryOperation(PreIncrementUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitPreDecrementUnaryOperation(PreDecrementUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitPostIncrementUnaryOperation(PostIncrementUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitPostDecrementUnaryOperation(PostDecrementUnaryOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitMethodRefNode(MethodRefNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitTypeRefNode(TypeRefNode node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitEqualityOperation(EqualityOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitAndOperation(AndOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitOrOperation(OrOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitConditionalExpression(ConditionalExpression node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitBetweenExpression(BetweenExpression node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitAdditiveOperation(AdditiveOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public EnJavaSpec visitMultiplicativeOperation(MultiplicativeOperation node) {
    // TODO Auto-generated method stub
    return null;
  }

}
