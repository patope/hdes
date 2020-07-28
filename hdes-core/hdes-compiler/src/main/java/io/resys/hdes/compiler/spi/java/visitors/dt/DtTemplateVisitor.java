package io.resys.hdes.compiler.spi.java.visitors.dt;

import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarTypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDefNode;
import io.resys.hdes.ast.api.nodes.AstNode.TypeName;
import io.resys.hdes.ast.api.nodes.AstNodeVisitor.DtAstNodeVisitor;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderRefValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyAll;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyFirst;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HitPolicyMatrix;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.InOperation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.LiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.MatrixRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.NegateLiteralValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.Rule;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.RuleRow;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.UndefinedValue;
import io.resys.hdes.ast.api.nodes.ExpressionNode.AndOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.BetweenExpression;
import io.resys.hdes.ast.api.nodes.ExpressionNode.EqualityOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.NotUnaryOperation;
import io.resys.hdes.ast.api.nodes.ExpressionNode.OrOperation;

public class DtTemplateVisitor<T, R> implements DtAstNodeVisitor<T, R> {
  @Override
  public T visitTypeName(TypeName node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLiteral(Literal node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitObjectDef(ObjectTypeDefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitScalarDef(ScalarTypeDefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public R visitDecisionTableBody(DecisionTableBody node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHeaders(Headers node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHeader(TypeDefNode node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHitPolicyAll(HitPolicyAll node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHitPolicyMatrix(HitPolicyMatrix node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHitPolicyFirst(HitPolicyFirst node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitRuleRow(RuleRow node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitRule(Rule node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitUndefinedValue(UndefinedValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitLiteralValue(LiteralValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitExpressionValue(ExpressionValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitEqualityOperation(EqualityOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitAndOperation(AndOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitOrOperation(OrOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitInOperation(InOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitNotOperation(NotUnaryOperation node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitHeaderRefValue(HeaderRefValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitBetweenExpression(BetweenExpression node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitNegateLiteralValue(NegateLiteralValue node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitMatrixRow(MatrixRow node) {
    throw new IllegalArgumentException("Not implemented");
  }

  @Override
  public T visitFormula(Headers node) {
    throw new IllegalArgumentException("Not implemented");
  }
}
