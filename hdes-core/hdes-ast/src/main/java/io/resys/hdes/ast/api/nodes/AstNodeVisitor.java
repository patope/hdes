package io.resys.hdes.ast.api.nodes;

import java.util.List;

import io.resys.hdes.ast.api.nodes.AstNode.Headers;
import io.resys.hdes.ast.api.nodes.AstNode.Literal;
import io.resys.hdes.ast.api.nodes.AstNode.ObjectDef;
import io.resys.hdes.ast.api.nodes.AstNode.ScalarDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeDef;
import io.resys.hdes.ast.api.nodes.AstNode.TypeInvocation;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.DecisionTableBody;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.ExpressionValue;
import io.resys.hdes.ast.api.nodes.DecisionTableNode.HeaderIndex;
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
import io.resys.hdes.ast.api.nodes.FlowNode.EndPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowBody;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowLoop;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskNode;
import io.resys.hdes.ast.api.nodes.FlowNode.FlowTaskPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.MappingValue;
import io.resys.hdes.ast.api.nodes.FlowNode.ThenPointer;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThen;
import io.resys.hdes.ast.api.nodes.FlowNode.WhenThenPointer;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Dropdown;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.DropdownField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Fields;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.FormField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Group;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.Groups;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.LiteralField;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskAction;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskActions;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskBody;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskDropdowns;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ManualTaskForm;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.ThenAction;
import io.resys.hdes.ast.api.nodes.ManualTaskNode.WhenAction;

public interface AstNodeVisitor<T, R> {
  // basic
  T visitTypeInvocation(TypeInvocation node);
  T visitLiteral(Literal node);

  interface TypeDefVisitor<T, R> extends AstNodeVisitor<T, R> {
    T visitObjectDef(ObjectDef node);
    T visitScalarDef(ScalarDef node);
  }
  
  // expression
  interface ExpressionAstNodeVisitor<T, R> extends AstNodeVisitor<T, R> { 
    R visitBody(ExpressionBody node);
    T visitNot(NotUnary node);
    T visitNegate(NegateUnary node);
    T visitPositive(PositiveUnary node);
    T visitPreIncrement(PreIncrementUnary node);
    T visitPreDecrement(PreDecrementUnary node);
    T visitPostIncrement(PostIncrementUnary node);
    T visitPostDecrement(PostDecrementUnary node);
    T visitMethod(MethodInvocation node);
    T visitEquality(EqualityOperation node);
    T visitAnd(AndExpression node);
    T visitOr(OrExpression node);
    T visitConditional(ConditionalExpression node);
    T visitBetween(BetweenExpression node);
    T visitAdditive(AdditiveExpression node);
    T visitMultiplicative(MultiplicativeExpression node);
    T visitLambda(LambdaExpression node);
  }
  
  // dt
  interface DtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitBody(DecisionTableBody node);
    T visitHeaders(Headers node);
    T visitHeader(TypeDef node);
    T visitHitPolicyAll(HitPolicyAll node);
    T visitHitPolicyMatrix(HitPolicyMatrix node);
    T visitHitPolicyFirst(HitPolicyFirst node);
    T visitRuleRow(RuleRow node);
    T visitRule(Rule node);
    T visitMatrixRow(MatrixRow node);
    T visitFormula(Headers node);
    
    T visitUndefinedValue(UndefinedValue node);
    T visitLiteralValue(LiteralValue node);
    T visitNegateLiteralValue(NegateLiteralValue node);
    
    T visitExpressionValue(ExpressionValue node);
    T visitEquality(EqualityOperation node);
    T visitBetween(BetweenExpression node);
    T visitAnd(AndExpression node);
    T visitOr(OrExpression node);
    T visitIn(InOperation node);
    T visitNot(NotUnary node);
    T visitHeaderIndex(HeaderIndex node);
  }
  
  // flow
  interface FlowAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitBody(FlowBody node);
    T visitInputs(List<TypeDef> node);
    T visitOutputs(List<TypeDef> node);
    T visitTask(FlowTaskNode node);
    
    T visitTaskPointer(FlowTaskNode parent, FlowTaskPointer node);
    T visitWhenThenPointer(FlowTaskNode parent, WhenThenPointer node);
    T visitThenPointer(FlowTaskNode parent, ThenPointer node);
    T visitEndPointer(FlowTaskNode parent, EndPointer node);
    
    T visitLoop(FlowLoop node);
    T visitWhenThen(WhenThen node);
    T visitWhen(ExpressionBody node);
    T visitMapping(FlowTaskNode node);
    T visitMappingValue(MappingValue node);
    T visitTaskRef(FlowTaskNode node);
  }
  
  // mt
  interface MtAstNodeVisitor<T, R> extends TypeDefVisitor<T, R> {
    R visitManualTaskBody(ManualTaskBody node);
    T visitManualTaskInputs(List<TypeDef> node);
    T visitManualTaskDropdowns(ManualTaskDropdowns node);
    T visitManualTaskStatements(ManualTaskActions node);
    T visitManualTaskForm(ManualTaskForm node);
    T visitDropdown(Dropdown node);
    T visitStatement(ManualTaskAction node);
    T visitWhenStatement(WhenAction node);
    T visitThenStatement(ThenAction node);
    T visitGroup(Group node);
    T visitGroups(Groups node);
    T visitFields(Fields node);
    T visitFormField(FormField node);
    T visitDropdownField(DropdownField node);
    T visitLiteralField(LiteralField node);
  }
}
